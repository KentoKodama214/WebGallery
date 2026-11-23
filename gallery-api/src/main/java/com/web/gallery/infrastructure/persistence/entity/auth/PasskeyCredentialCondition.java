package com.web.gallery.infrastructure.persistence.entity.auth;

import lombok.Builder;
import lombok.Data;

/** パスキーテーブルの抽出条件クラス */
@Data
@Builder
public class PasskeyCredentialCondition {
  /** アカウント番号 */
  private Long accountNo;

  /** パスキー番号 */
  private Long passkeyNo;

  /** クレデンシャルID */
  private String credentialId;

  /**
   * アカウント番号で抽出条件を生成する
   *
   * @param accountNo アカウント番号
   * @return {@link PasskeyCredentialCondition}
   */
  public static PasskeyCredentialCondition byAccountNo(Long accountNo) {
    return PasskeyCredentialCondition.builder().accountNo(accountNo).build();
  }

  /**
   * クレデンシャルIDで抽出条件を生成する
   *
   * @param credentialId クレデンシャルID
   * @return {@link PasskeyCredentialCondition}
   */
  public static PasskeyCredentialCondition byCredentialId(String credentialId) {
    return PasskeyCredentialCondition.builder().credentialId(credentialId).build();
  }

  /**
   * パスキー番号で抽出条件を生成する（認証結果の反映用。所有者は取得済みのため条件に含めない）
   *
   * @param passkeyNo パスキー番号
   * @return {@link PasskeyCredentialCondition}
   */
  public static PasskeyCredentialCondition byPasskeyNo(Long passkeyNo) {
    return PasskeyCredentialCondition.builder().passkeyNo(passkeyNo).build();
  }

  /**
   * アカウント番号・パスキー番号で抽出条件を生成する（他人のパスキーを操作できないよう所有者を条件に含める）
   *
   * @param accountNo アカウント番号
   * @param passkeyNo パスキー番号
   * @return {@link PasskeyCredentialCondition}
   */
  public static PasskeyCredentialCondition byAccountNoAndPasskeyNo(Long accountNo, Long passkeyNo) {
    return PasskeyCredentialCondition.builder().accountNo(accountNo).passkeyNo(passkeyNo).build();
  }
}
