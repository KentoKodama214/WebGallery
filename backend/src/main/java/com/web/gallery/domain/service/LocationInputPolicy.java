package com.web.gallery.domain.service;

import com.web.gallery.domain.model.common.GeoLocation;
import com.web.gallery.domain.model.common.LocationDisplayName;
import com.web.gallery.domain.model.common.LocationManagementName;
import com.web.gallery.domain.model.photo.LocationNo;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/** ロケーション情報の入力に関するビジネスルールを判定するドメインサービス */
@Component
public class LocationInputPolicy {

  /** 緯度の下限 */
  private static final BigDecimal LATITUDE_MIN = new BigDecimal("-90");

  /** 緯度の上限 */
  private static final BigDecimal LATITUDE_MAX = new BigDecimal("90");

  /** 経度の下限 */
  private static final BigDecimal LONGITUDE_MIN = new BigDecimal("-180");

  /** 経度の上限 */
  private static final BigDecimal LONGITUDE_MAX = new BigDecimal("180");

  /**
   * ロケーション情報の入力が整合しているかどうかを判定する
   *
   * <p>ロケーション番号（既存マスタからの選択）が指定されている場合は、管理名・表示名・緯度・経度の入力は問わない
   * （サーバ側はロケーション番号のみを信頼するため）。ロケーション番号が未指定で管理名・表示名のいずれかが
   * 指定されている場合（新規入力）は、管理名・表示名・緯度・経度のすべてが指定され、かつ緯度・経度が
   * 地理的に有効な範囲内であることを要求する。いずれも未指定の場合はロケーション未設定として許可する
   *
   * <p>緯度・経度の範囲チェックはRequest側のBean Validationと重複するが、範囲外の値は {@code common.location_mst}の列定義（{@code
   * decimal(11,4)}）の桁数を超えて {@code numeric field overflow}（500）になりうるため、ドメインの不変条件としても守る。
   * 小数第5位以下は検証せず、従来どおりDB側で丸める（範囲内に収まっていれば桁数はあふれない）
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
        && geoLocation.longitude() != null
        && isInRange(geoLocation.latitude().value(), LATITUDE_MIN, LATITUDE_MAX)
        && isInRange(geoLocation.longitude().value(), LONGITUDE_MIN, LONGITUDE_MAX);
  }

  /**
   * 値が下限・上限の範囲内（両端を含む）かどうかを判定する
   *
   * @param value 判定対象の値
   * @param min 下限
   * @param max 上限
   * @return 範囲内の場合、true
   */
  private boolean isInRange(BigDecimal value, BigDecimal min, BigDecimal max) {
    return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
  }
}
