package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * 位置情報（住所・緯度・経度）の値オブジェクト
 *
 * @param address 住所
 * @param latitude 緯度
 * @param longitude 経度
 */
public record GeoLocation(Address address, Latitude latitude, Longitude longitude)
    implements Serializable {

  /**
   * 全項目が未設定のGeoLocationを生成する
   *
   * @return {@link GeoLocation}
   */
  public static GeoLocation empty() {
    return new GeoLocation(null, null, null);
  }
}
