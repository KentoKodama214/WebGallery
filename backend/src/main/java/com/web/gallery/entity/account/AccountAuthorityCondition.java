package com.web.gallery.entity.account;

import lombok.Builder;
import lombok.Data;

/** アカウント権限テーブルの抽出条件クラス */
@Data
@Builder
public class AccountAuthorityCondition {
  /** アカウント番号 */
  private Long accountNo;

  /**
   * アカウント番号で検索するための抽出条件を生成する
   *
   * @param accountNo アカウント番号
   * @return {@link AccountAuthorityCondition}
   */
  public static AccountAuthorityCondition byAccountNo(Long accountNo) {
    return AccountAuthorityCondition.builder().accountNo(accountNo).build();
  }
}
