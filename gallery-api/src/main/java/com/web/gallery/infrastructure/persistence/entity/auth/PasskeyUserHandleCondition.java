package com.web.gallery.infrastructure.persistence.entity.auth;

import lombok.Builder;
import lombok.Data;

/** パスキーユーザーハンドルテーブルの抽出条件クラス */
@Data
@Builder
public class PasskeyUserHandleCondition {
  /** アカウント番号 */
  private Long accountNo;

  /**
   * アカウント番号で抽出条件を生成する
   *
   * @param accountNo アカウント番号
   * @return {@link PasskeyUserHandleCondition}
   */
  public static PasskeyUserHandleCondition byAccountNo(Long accountNo) {
    return PasskeyUserHandleCondition.builder().accountNo(accountNo).build();
  }
}
