package com.web.gallery.helper;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.web.gallery.domain.photo.ExifData;
import com.web.gallery.domain.photo.FValue;
import com.web.gallery.domain.photo.FocalLength;
import com.web.gallery.domain.photo.Iso;
import com.web.gallery.domain.photo.ShutterSpeed;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * アップロードされた画像ファイルの実バイナリに埋め込まれたEXIF情報（焦点距離・F値・シャッタースピード・ISO）を抽出するHelperクラス
 *
 * <p>クライアントが申告する値をそのまま信頼せず、画像ファイル自体から読み取った実際の値を優先して採用するために使用する。
 * PNG等EXIFを保持しない形式や、EXIFはあっても各項目が記録されていない画像では項目ごとに未設定を返すため、 呼び出し元でクライアント申告値へのフォールバックが必要となる
 */
@Component
public class PhotoExifExtractor {

  /**
   * 画像ファイルからEXIF情報を抽出する
   *
   * <p>破損した画像や未知の形式、EXIF未埋め込みの画像（PNG等）では、全項目が未設定の{@link ExifData}を返す。
   * ライブラリの解析中に想定外の例外が発生した場合も同様に扱う（不正な画像でリクエスト処理自体を失敗させないため）
   *
   * @param imageFile 抽出対象の画像ファイル
   * @return {@link ExifData}
   */
  public ExifData extract(MultipartFile imageFile) {
    try (InputStream inputStream = imageFile.getInputStream()) {
      Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
      ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
      if (directory == null) {
        return ExifData.empty();
      }
      return new ExifData(
          extractFocalLength(directory),
          extractFValue(directory),
          extractShutterSpeed(directory),
          extractIso(directory));
    } catch (IOException | ImageProcessingException | RuntimeException e) {
      // 破損画像・未対応形式・ライブラリ内部の想定外エラーはEXIF情報なし扱いとし、リクエスト処理は継続する
      return ExifData.empty();
    }
  }

  /**
   * 焦点距離（mm）を抽出する
   *
   * @param directory {@link ExifSubIFDDirectory}
   * @return 取得できた場合は{@link FocalLength}、できない場合はnull
   */
  private FocalLength extractFocalLength(ExifSubIFDDirectory directory) {
    Double value = directory.getDoubleObject(ExifSubIFDDirectory.TAG_FOCAL_LENGTH);
    return value != null && value > 0 ? new FocalLength((int) Math.round(value)) : null;
  }

  /**
   * F値を抽出する
   *
   * @param directory {@link ExifSubIFDDirectory}
   * @return 取得できた場合は{@link FValue}、できない場合はnull
   */
  private FValue extractFValue(ExifSubIFDDirectory directory) {
    Double value = directory.getDoubleObject(ExifSubIFDDirectory.TAG_FNUMBER);
    return value != null && value > 0 ? new FValue(BigDecimal.valueOf(value)) : null;
  }

  /**
   * シャッタースピード（秒）を抽出する
   *
   * @param directory {@link ExifSubIFDDirectory}
   * @return 取得できた場合は{@link ShutterSpeed}、できない場合はnull
   */
  private ShutterSpeed extractShutterSpeed(ExifSubIFDDirectory directory) {
    Double value = directory.getDoubleObject(ExifSubIFDDirectory.TAG_EXPOSURE_TIME);
    return value != null && value > 0 ? new ShutterSpeed(BigDecimal.valueOf(value)) : null;
  }

  /**
   * ISO感度を抽出する
   *
   * @param directory {@link ExifSubIFDDirectory}
   * @return 取得できた場合は{@link Iso}、できない場合はnull
   */
  private Iso extractIso(ExifSubIFDDirectory directory) {
    Integer value = directory.getInteger(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT);
    return value != null && value > 0 ? new Iso(value) : null;
  }
}
