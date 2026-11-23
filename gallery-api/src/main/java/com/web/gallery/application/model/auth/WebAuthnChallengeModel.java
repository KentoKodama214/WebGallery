package com.web.gallery.application.model.auth;

import com.web.gallery.domain.enumeration.WebAuthnChallengePurposeEnum;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.auth.PasskeyChallenge;
import com.web.gallery.domain.model.common.ExpiresAt;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * WebAuthnチャレンジを受け渡すためのModelクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@Builder
public class WebAuthnChallengeModel {
  /** チャレンジ */
  @NonNull private PasskeyChallenge challenge;

  /** 用途区分 */
  @NonNull private WebAuthnChallengePurposeEnum purposeKbn;

  /** アカウント番号（認証用は認証前でアカウントが未確定のためnull） */
  private AccountNo accountNo;

  /** 有効期限 */
  @NonNull private ExpiresAt expiresAt;

  /**
   * 指定した用途のWebAuthnChallengeModelを生成する
   *
   * @param challenge チャレンジ
   * @param purposeKbn 用途区分
   * @param accountNo アカウント番号（認証用はnull）
   * @param expiresAt 有効期限
   * @return {@link WebAuthnChallengeModel}
   */
  public static WebAuthnChallengeModel of(
      PasskeyChallenge challenge,
      WebAuthnChallengePurposeEnum purposeKbn,
      AccountNo accountNo,
      ExpiresAt expiresAt) {
    return WebAuthnChallengeModel.builder()
        .challenge(challenge)
        .purposeKbn(purposeKbn)
        .accountNo(accountNo)
        .expiresAt(expiresAt)
        .build();
  }
}
