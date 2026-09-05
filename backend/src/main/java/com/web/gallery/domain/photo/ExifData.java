package com.web.gallery.domain.photo;

import java.io.Serializable;

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
}
