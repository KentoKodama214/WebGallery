package com.web.gallery.infrastructure.persistence.entity.auth;

import lombok.Builder;
import lombok.Data;

/** リカバリーコードテーブルの抽出条件クラス */
@Data
@Builder
public class RecoveryCodeCondition {
  /** アカウント番号 */
  private Long accountNo;

  /** コードハッシュ */
  private String codeHash;

  /**
   * アカウント番号で抽出条件を生成する
   *
   * @param accountNo アカウント番号
   * @return {@link RecoveryCodeCondition}
   */
  public static RecoveryCodeCondition byAccountNo(Long accountNo) {
    return RecoveryCodeCondition.builder().accountNo(accountNo).build();
  }

  /**
   * アカウント番号・コードハッシュで消費（照合と同時に削除）用の抽出条件を生成する
   *
   * @param accountNo アカウント番号
   * @param codeHash コードハッシュ
   * @return {@link RecoveryCodeCondition}
   */
  public static RecoveryCodeCondition byAccountNoAndCodeHash(Long accountNo, String codeHash) {
    return RecoveryCodeCondition.builder().accountNo(accountNo).codeHash(codeHash).build();
  }
}
