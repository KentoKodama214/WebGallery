package com.web.gallery.helper;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.photo.ExifData;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PhotoExifExtractorTest {
  private final PhotoExifExtractor photoExifExtractor = new PhotoExifExtractor();

  /**
   * RATIONAL型（8バイト：分子4バイト＋分母4バイト、リトルエンディアン）のバイト列を生成する
   *
   * @param numerator 分子
   * @param denominator 分母
   * @return バイト列
   */
  private static byte[] rational(int numerator, int denominator) {
    return ByteBuffer.allocate(8)
        .order(ByteOrder.LITTLE_ENDIAN)
        .putInt(numerator)
        .putInt(denominator)
        .array();
  }

  /**
   * TIFF IFDエントリ（12バイト：タグ2＋型2＋個数4＋値またはオフセット4、リトルエンディアン）のバイト列を生成する
   *
   * @param tag タグ番号
   * @param type 値の型（TIFF型番号）
   * @param count 値の個数
   * @param valueOrOffset 値そのもの、または外部格納値へのオフセット
   * @return バイト列
   */
  private static byte[] ifdEntry(int tag, int type, int count, int valueOrOffset) {
    return ByteBuffer.allocate(12)
        .order(ByteOrder.LITTLE_ENDIAN)
        .putShort((short) tag)
        .putShort((short) type)
        .putInt(count)
        .putInt(valueOrOffset)
        .array();
  }

  /**
   * 指定したEXIF項目（焦点距離・F値・シャッタースピード・ISO）を埋め込んだJPEG APP1セグメントを生成する
   *
   * <p>ExifSubIFDに対する1エントリのIFD0＋4エントリのExifSubIFDからなる最小限のTIFF構造を組み立てる。 未指定の項目は{@code
   * null}を渡すとタグ自体を含めない（EXIFに項目が存在しない状態を再現する）
   *
   * @param focalLengthMm 焦点距離（mm）。未指定の場合null
   * @param fNumber F値。未指定の場合null
   * @param exposureTimeSec シャッタースピード（秒）。未指定の場合null
   * @param iso ISO感度。未指定の場合null
   * @return APP1セグメントのバイト列
   */
  private static byte[] buildExifApp1(
      Integer focalLengthMm, Double fNumber, Double exposureTimeSec, Integer iso) throws Exception {
    record Entry(int tag, int type, byte[] externalValue, Integer directValue) {}
    java.util.List<Entry> entries = new java.util.ArrayList<>();
    if (focalLengthMm != null) {
      entries.add(new Entry(0x920A, 5, rational(focalLengthMm, 1), null));
    }
    if (fNumber != null) {
      entries.add(new Entry(0x829D, 5, rational((int) Math.round(fNumber * 10), 10), null));
    }
    if (exposureTimeSec != null) {
      entries.add(new Entry(0x829A, 5, rational(1, (int) Math.round(1 / exposureTimeSec)), null));
    }
    if (iso != null) {
      entries.add(new Entry(0x8827, 3, null, iso));
    }

    ByteArrayOutputStream tiff = new ByteArrayOutputStream();
    // TIFFヘッダー: リトルエンディアン＋マジックナンバー＋IFD0オフセット(8)
    tiff.write(new byte[] {0x49, 0x49, 0x2A, 0x00, 0x08, 0x00, 0x00, 0x00});

    int exifSubIfdOffset = 8 + (2 + 12 + 4);
    tiff.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) 1).array());
    tiff.write(ifdEntry(0x8769, 4, 1, exifSubIfdOffset)); // ExifIFDポインタ
    tiff.write(new byte[] {0, 0, 0, 0});

    int externalValuesOffset = exifSubIfdOffset + (2 + entries.size() * 12 + 4);
    tiff.write(
        ByteBuffer.allocate(2)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putShort((short) entries.size())
            .array());
    int externalOffset = externalValuesOffset;
    for (Entry entry : entries) {
      if (entry.externalValue() != null) {
        tiff.write(ifdEntry(entry.tag(), entry.type(), 1, externalOffset));
        externalOffset += entry.externalValue().length;
      } else {
        tiff.write(ifdEntry(entry.tag(), entry.type(), 1, entry.directValue()));
      }
    }
    tiff.write(new byte[] {0, 0, 0, 0});
    for (Entry entry : entries) {
      if (entry.externalValue() != null) {
        tiff.write(entry.externalValue());
      }
    }

    byte[] tiffBytes = tiff.toByteArray();
    ByteArrayOutputStream app1 = new ByteArrayOutputStream();
    app1.write(new byte[] {(byte) 0xFF, (byte) 0xE1});
    int segmentLength = 2 + 6 + tiffBytes.length;
    app1.write(
        ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).putShort((short) segmentLength).array());
    app1.write("Exif\0\0".getBytes(StandardCharsets.US_ASCII));
    app1.write(tiffBytes);
    return app1.toByteArray();
  }

  /**
   * 指定したEXIF項目を埋め込んだJPEGファイルを表す{@link MultipartFile}を生成する
   *
   * @param focalLengthMm 焦点距離（mm）。未指定の場合null
   * @param fNumber F値。未指定の場合null
   * @param exposureTimeSec シャッタースピード（秒）。未指定の場合null
   * @param iso ISO感度。未指定の場合null
   * @return {@link MultipartFile}
   */
  private static MultipartFile createJpegWithExif(
      Integer focalLengthMm, Double fNumber, Double exposureTimeSec, Integer iso) throws Exception {
    BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream baseJpeg = new ByteArrayOutputStream();
    ImageIO.write(image, "jpg", baseJpeg);
    byte[] baseBytes = baseJpeg.toByteArray();

    byte[] app1Segment = buildExifApp1(focalLengthMm, fNumber, exposureTimeSec, iso);

    // SOIマーカー（FF D8）の直後にAPP1（EXIF）セグメントを挿入する
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    result.write(baseBytes, 0, 2);
    result.write(app1Segment);
    result.write(baseBytes, 2, baseBytes.length - 2);
    return new MockMultipartFile("imageFile", "test.jpg", "image/jpeg", result.toByteArray());
  }

  @Test
  @Order(1)
  @DisplayName("正常系：EXIF情報（焦点距離・F値・シャッタースピード・ISO）が埋め込まれたJPEGから全項目を抽出できる")
  void extract_allFieldsPresent() throws Exception {
    MultipartFile imageFile = createJpegWithExif(50, 2.8, 0.004, 200);

    ExifData exifData = photoExifExtractor.extract(imageFile);

    assertEquals(50, exifData.focalLength().value());
    assertEquals(0, BigDecimal.valueOf(2.8).compareTo(exifData.fValue().value()));
    assertEquals(0, BigDecimal.valueOf(0.004).compareTo(exifData.shutterSpeed().value()));
    assertEquals(200, exifData.iso().value());
  }

  @Test
  @Order(2)
  @DisplayName("正常系：EXIFの一部の項目のみ記録されている場合、記録されている項目のみ抽出し、他はnullを返す")
  void extract_partialFieldsPresent() throws Exception {
    MultipartFile imageFile = createJpegWithExif(50, null, null, null);

    ExifData exifData = photoExifExtractor.extract(imageFile);

    assertEquals(50, exifData.focalLength().value());
    assertNull(exifData.fValue());
    assertNull(exifData.shutterSpeed());
    assertNull(exifData.iso());
  }

  @Test
  @Order(3)
  @DisplayName("正常系：EXIFが埋め込まれていないJPEGの場合、全項目未設定のExifDataを返す")
  void extract_noExif_returnsEmpty() throws Exception {
    BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "jpg", outputStream);
    MultipartFile imageFile =
        new MockMultipartFile("imageFile", "test.jpg", "image/jpeg", outputStream.toByteArray());

    assertEquals(ExifData.empty(), photoExifExtractor.extract(imageFile));
  }

  @Test
  @Order(4)
  @DisplayName("正常系：PNG形式（EXIF非対応）の場合、全項目未設定のExifDataを返す")
  void extract_png_returnsEmpty() throws Exception {
    BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "png", outputStream);
    MultipartFile imageFile =
        new MockMultipartFile("imageFile", "test.png", "image/png", outputStream.toByteArray());

    assertEquals(ExifData.empty(), photoExifExtractor.extract(imageFile));
  }

  @Test
  @Order(5)
  @DisplayName("異常系：破損した不正なバイト列の場合、全項目未設定のExifDataを返す")
  void extract_invalidBytes_returnsEmpty() {
    MultipartFile imageFile =
        new MockMultipartFile("imageFile", "test.jpg", "image/jpeg", "not an image".getBytes());

    assertEquals(ExifData.empty(), photoExifExtractor.extract(imageFile));
  }

  @Test
  @Order(6)
  @DisplayName("異常系：EXIFの値が0（焦点距離・F値・ISO）の場合、無効な値として無視されnullを返す")
  void extract_zeroValues_ignored() throws Exception {
    MultipartFile imageFile = createJpegWithExif(0, 0.0, null, 0);

    ExifData exifData = photoExifExtractor.extract(imageFile);

    assertNull(exifData.focalLength());
    assertNull(exifData.fValue());
    assertNull(exifData.iso());
  }

  @Test
  @Order(7)
  @DisplayName("異常系：画像読み込み中にIOExceptionが発生した場合、全項目未設定のExifDataを返す")
  void extract_ioException_returnsEmpty() throws Exception {
    MultipartFile imageFile = org.mockito.Mockito.mock(MultipartFile.class);
    org.mockito.Mockito.doThrow(new java.io.IOException("read error"))
        .when(imageFile)
        .getInputStream();

    assertEquals(ExifData.empty(), photoExifExtractor.extract(imageFile));
  }
}
