package com.web.gallery.helper;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.enumeration.DirectionEnum;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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
public class PhotoDirectionResolverTest {
  private final PhotoDirectionResolver photoDirectionResolver = new PhotoDirectionResolver();

  /**
   * 指定のピクセルサイズを持つ実際のJPEGファイルを表す{@link MultipartFile}を生成する
   *
   * @param width 幅（ピクセル）
   * @param height 高さ（ピクセル）
   * @return {@link MultipartFile}
   */
  private static MultipartFile createJpegFile(int width, int height) throws Exception {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "jpg", outputStream);
    return new MockMultipartFile("imageFile", "test.jpg", "image/jpeg", outputStream.toByteArray());
  }

  @Test
  @Order(1)
  @DisplayName("正常系：幅より高さが大きい場合、VERTICALを返す")
  void resolve_vertical() throws Exception {
    assertEquals(DirectionEnum.VERTICAL, photoDirectionResolver.resolve(createJpegFile(100, 200)));
  }

  @Test
  @Order(2)
  @DisplayName("正常系：高さより幅が大きい場合、HORIZONTALを返す")
  void resolve_horizontal() throws Exception {
    assertEquals(
        DirectionEnum.HORIZONTAL, photoDirectionResolver.resolve(createJpegFile(200, 100)));
  }

  @Test
  @Order(3)
  @DisplayName("正常系：幅と高さが等しい場合、SQUAREを返す")
  void resolve_square() throws Exception {
    assertEquals(DirectionEnum.SQUARE, photoDirectionResolver.resolve(createJpegFile(150, 150)));
  }

  @Test
  @Order(4)
  @DisplayName("正常系：PNG形式でも判定できる")
  void resolve_png() throws Exception {
    BufferedImage image = new BufferedImage(120, 80, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "png", outputStream);
    MultipartFile pngFile =
        new MockMultipartFile("imageFile", "test.png", "image/png", outputStream.toByteArray());

    assertEquals(DirectionEnum.HORIZONTAL, photoDirectionResolver.resolve(pngFile));
  }

  @Test
  @Order(5)
  @DisplayName("異常系：ピクセルサイズを判定できない不正なバイト列の場合、NONEを返す")
  void resolve_invalidBytes_returnsNone() {
    MultipartFile invalidFile =
        new MockMultipartFile("imageFile", "test.jpg", "image/jpeg", "not an image".getBytes());

    assertEquals(DirectionEnum.NONE, photoDirectionResolver.resolve(invalidFile));
  }
}
