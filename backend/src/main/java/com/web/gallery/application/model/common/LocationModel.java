package com.web.gallery.application.model.common;

import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.common.Address;
import com.web.gallery.domain.model.common.GeoLocation;
import com.web.gallery.domain.model.common.Latitude;
import com.web.gallery.domain.model.common.LocationDisplayName;
import com.web.gallery.domain.model.common.LocationManagementName;
import com.web.gallery.domain.model.common.Longitude;
import com.web.gallery.domain.model.photo.LocationNo;
import com.web.gallery.infrastructure.persistence.entity.common.LocationMst;
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

  /** 管理名 */
  @NonNull private LocationManagementName managementName;

  /** 表示名 */
  @NonNull private LocationDisplayName displayName;

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
        .managementName(new LocationManagementName(entity.getManagementName()))
        .displayName(new LocationDisplayName(entity.getDisplayName()))
        .geoLocation(
            new GeoLocation(
                new Address(entity.getAddress()),
                new Latitude(entity.getLatitude()),
                new Longitude(entity.getLongitude())))
        .build();
  }
}
