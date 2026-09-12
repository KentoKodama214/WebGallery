package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * IPアドレスから解決した位置情報（国・地域）の値オブジェクト
 *
 * @param country 国
 * @param region 地域
 */
public record IpGeoLocation(Country country, Region region) implements Serializable {

  /**
   * 全項目が未解決のIpGeoLocationを生成する
   *
   * @return {@link IpGeoLocation}
   */
  public static IpGeoLocation empty() {
    return new IpGeoLocation(null, null);
  }
}
