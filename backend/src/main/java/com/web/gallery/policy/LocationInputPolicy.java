package com.web.gallery.policy;

import com.web.gallery.domain.common.GeoLocation;
import com.web.gallery.domain.common.LocationDisplayName;
import com.web.gallery.domain.common.LocationManagementName;
import com.web.gallery.domain.photo.LocationNo;
import org.springframework.stereotype.Component;

/** ロケーション情報の入力に関するビジネスルールを判定するドメインサービス */
@Component
public class LocationInputPolicy {

  /**
   * ロケーション情報の入力が整合しているかどうかを判定する
   *
   * <p>ロケーション番号（既存マスタからの選択）が指定されている場合は、管理名・表示名・緯度・経度の入力は問わない
   * （サーバ側はロケーション番号のみを信頼するため）。ロケーション番号が未指定で管理名・表示名のいずれかが
   * 指定されている場合（新規入力）は、管理名・表示名・緯度・経度のすべてが指定されていることを要求する。 いずれも未指定の場合はロケーション未設定として許可する
   *
   * @param locationNo ロケーション番号
   * @param managementName 管理名
   * @param displayName 表示名
   * @param geoLocation 位置情報（住所・緯度・経度）
   * @return 入力が整合している場合、true
   */
  public Boolean isValid(
      LocationNo locationNo,
      LocationManagementName managementName,
      LocationDisplayName displayName,
      GeoLocation geoLocation) {
    boolean isExistingSelection = locationNo != null && locationNo.value() > 0;
    if (isExistingSelection) {
      return true;
    }

    boolean isNewInput = managementName != null || displayName != null;
    if (!isNewInput) {
      return true;
    }

    return managementName != null
        && displayName != null
        && geoLocation.latitude() != null
        && geoLocation.longitude() != null;
  }
}
