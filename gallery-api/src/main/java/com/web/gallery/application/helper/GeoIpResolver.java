package com.web.gallery.application.helper;

import com.web.gallery.domain.model.common.IpAddress;
import com.web.gallery.domain.model.common.IpGeoLocation;

/**
 * IPアドレスから国・地域を解決するインターフェース
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
public interface GeoIpResolver {

  /**
   * IPアドレスから国・地域を解決する
   *
   * @param ipAddress IPアドレス
   * @return {@link IpGeoLocation}（解決できない場合は{@link IpGeoLocation#empty()}）
   */
  IpGeoLocation resolve(IpAddress ipAddress);
}
