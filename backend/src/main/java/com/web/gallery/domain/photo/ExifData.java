package com.web.gallery.domain.photo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * EXIF情報（撮影メタデータ）の値オブジェクト
 *
 * @param focalLength 焦点距離
 * @param fValue F値
 * @param shutterSpeed シャッタースピード
 * @param iso ISO
 */
public record ExifData(FocalLength focalLength, FValue fValue, ShutterSpeed shutterSpeed, Iso iso)
    implements Serializable {

  /**
   * 全項目が未設定のExifDataを生成する
   *
   * @return {@link ExifData}
   */
  public static ExifData empty() {
    return new ExifData(null, null, null, null);
  }

  /**
   * 未設定を表すnullを許容するプリミティブな値からExifDataを生成する
   *
   * <p>クライアント申告値（リクエストDTOのフィールド）から生成する用途で使用する
   *
   * @param focalLength 焦点距離。未設定の場合null
   * @param fValue F値。未設定の場合null
   * @param shutterSpeed シャッタースピード。未設定の場合null
   * @param iso ISO。未設定の場合null
   * @return {@link ExifData}
   */
  public static ExifData fromRawValues(
      Integer focalLength, BigDecimal fValue, BigDecimal shutterSpeed, Integer iso) {
    return new ExifData(
        focalLength != null ? new FocalLength(focalLength) : null,
        fValue != null ? new FValue(fValue) : null,
        shutterSpeed != null ? new ShutterSpeed(shutterSpeed) : null,
        iso != null ? new Iso(iso) : null);
  }
}
