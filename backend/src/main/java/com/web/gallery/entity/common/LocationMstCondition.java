package com.web.gallery.entity.common;

import lombok.Builder;
import lombok.Data;

/** ロケーションマスタテーブルの抽出条件クラス */
@Data
@Builder
public class LocationMstCondition {
  /** アカウント番号 */
  private Long accountNo;

  /** ロケーション番号 */
  private Long locationNo;

  /** ロケーション名 */
  private String locationName;

  /**
   * アカウント番号による抽出条件を生成する
   *
   * @param accountNo アカウント番号
   * @return {@link LocationMstCondition}
   */
  public static LocationMstCondition byAccountNo(Long accountNo) {
    return LocationMstCondition.builder().accountNo(accountNo).build();
  }

  /**
   * アカウント番号・ロケーション番号による抽出条件を生成する
   *
   * <p>クライアントが選択した既存ロケーションが本人所有かどうかの検証に使用する
   *
   * @param accountNo アカウント番号
   * @param locationNo ロケーション番号
   * @return {@link LocationMstCondition}
   */
  public static LocationMstCondition byAccountAndLocationNo(Long accountNo, Long locationNo) {
    return LocationMstCondition.builder().accountNo(accountNo).locationNo(locationNo).build();
  }

  /**
   * アカウント番号・ロケーション名による抽出条件を生成する
   *
   * <p>新規ロケーション登録時の重複チェック（同名ロケーションの再利用）に使用する
   *
   * @param accountNo アカウント番号
   * @param locationName ロケーション名
   * @return {@link LocationMstCondition}
   */
  public static LocationMstCondition byAccountAndName(Long accountNo, String locationName) {
    return LocationMstCondition.builder().accountNo(accountNo).locationName(locationName).build();
  }
}
