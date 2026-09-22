package com.web.gallery.entity.common;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.GeoLocation;
import com.web.gallery.domain.common.LocationName;
import com.web.gallery.domain.photo.LocationNo;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

/** ロケーションマスタテーブルのEntityクラス */
@Data
@Builder
public class LocationMst {
  /** ID */
  private Long id;

  /** アカウント番号 */
  private Long accountNo;

  /** ロケーション番号 */
  private Long locationNo;

  /** 作成者 */
  private Long createdBy;

  /** 作成日時 */
  private OffsetDateTime createdAt;

  /** ロケーション名 */
  private String locationName;

  /** 住所 */
  private String address;

  /** 緯度 */
  private BigDecimal latitude;

  /** 経度 */
  private BigDecimal longitude;

  /**
   * 新規登録用のLocationMstエンティティを生成する
   *
   * @param accountNo アカウント番号
   * @param newLocationNo 新規採番されたロケーション番号
   * @param locationName ロケーション名
   * @param geoLocation 位置情報（住所・緯度・経度）
   * @return {@link LocationMst}
   */
  public static LocationMst fromForRegist(
      AccountNo accountNo,
      LocationNo newLocationNo,
      LocationName locationName,
      GeoLocation geoLocation) {
    return LocationMst.builder()
        .accountNo(accountNo.value())
        .locationNo(newLocationNo.value())
        .createdBy(accountNo.value())
        .locationName(locationName.value())
        .address(geoLocation.address() != null ? geoLocation.address().value() : "")
        .latitude(geoLocation.latitude().value())
        .longitude(geoLocation.longitude().value())
        .build();
  }
}
