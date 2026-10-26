package com.web.gallery.application.model.common;

import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.common.GeoLocation;
import com.web.gallery.domain.model.common.LocationDisplayName;
import com.web.gallery.domain.model.common.LocationManagementName;
import com.web.gallery.domain.model.photo.LocationNo;
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
}
