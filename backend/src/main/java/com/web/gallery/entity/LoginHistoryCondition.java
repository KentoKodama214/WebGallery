package com.web.gallery.entity;

import lombok.Builder;
import lombok.Data;

/** ログイン履歴テーブルの抽出条件クラス */
@Data
@Builder
public class LoginHistoryCondition {
  /** アカウント番号 */
  private Long accountNo;

  /**
   * アカウント番号で削除用の抽出条件を生成する
   *
   * @param accountNo アカウント番号
   * @return {@link LoginHistoryCondition}
   */
  public static LoginHistoryCondition byAccountNo(Long accountNo) {
    return LoginHistoryCondition.builder().accountNo(accountNo).build();
  }
}
