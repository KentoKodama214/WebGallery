package com.web.gallery.model.common;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.Address;
import com.web.gallery.domain.common.GeoLocation;
import com.web.gallery.domain.common.Latitude;
import com.web.gallery.domain.common.LocationName;
import com.web.gallery.domain.common.Longitude;
import com.web.gallery.domain.photo.LocationNo;
import com.web.gallery.entity.common.LocationMst;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** ロケーションマスタの情報を受け渡すためのModelクラス */
@Value
@Builder
public class LocationModel {
  /** アカウント番号 */
  @NonNull private AccountNo accountNo;

  /** ロケーション番号 */
  @NonNull private LocationNo locationNo;

  /** ロケーション名 */
  @NonNull private LocationName locationName;

  /** 位置情報（住所・緯度・経度） */
  @NonNull private GeoLocation geoLocation;

  /**
   * LocationMstエンティティからLocationModelを生成する
   *
   * @param entity {@link LocationMst}
   * @return {@link LocationModel}
   */
  public static LocationModel from(LocationMst entity) {
    return LocationModel.builder()
        .accountNo(new AccountNo(entity.getAccountNo()))
        .locationNo(new LocationNo(entity.getLocationNo()))
        .locationName(new LocationName(entity.getLocationName()))
        .geoLocation(
            new GeoLocation(
                new Address(entity.getAddress()),
                new Latitude(entity.getLatitude()),
                new Longitude(entity.getLongitude())))
        .build();
  }
}
