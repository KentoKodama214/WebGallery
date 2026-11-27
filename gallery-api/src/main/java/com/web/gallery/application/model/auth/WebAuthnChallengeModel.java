package com.web.gallery.application.model.auth;

import com.web.gallery.domain.enumeration.WebAuthnChallengePurposeEnum;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.auth.PasskeyChallenge;
import com.web.gallery.domain.model.auth.PasskeyUserHandle;
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

  /** アカウント番号（認証用・新規登録用はアカウントが未確定（未作成）のためnull） */
  private AccountNo accountNo;

  /** ユーザーハンドル（新規登録用のみ。登録オプションで払い出したWebAuthnのuser.id。それ以外はnull） */
  private PasskeyUserHandle userHandle;

  /** 有効期限 */
  @NonNull private ExpiresAt expiresAt;

  /**
   * 指定した用途のWebAuthnChallengeModelを生成する
   *
   * @param challenge チャレンジ
   * @param purposeKbn 用途区分
   * @param accountNo アカウント番号（認証用・新規登録用はnull）
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

  /**
   * アカウント新規登録用のWebAuthnChallengeModelを生成する
   *
   * <p>アカウントは未作成のためアカウント番号を持たず、代わりに登録オプションで払い出したユーザーハンドルを保持する （登録完了時に同じユーザーハンドルでアカウントへ紐づけるため）
   *
   * @param challenge チャレンジ
   * @param userHandle ユーザーハンドル
   * @param expiresAt 有効期限
   * @return {@link WebAuthnChallengeModel}
   */
  public static WebAuthnChallengeModel forSignup(
      PasskeyChallenge challenge, PasskeyUserHandle userHandle, ExpiresAt expiresAt) {
    return WebAuthnChallengeModel.builder()
        .challenge(challenge)
        .purposeKbn(WebAuthnChallengePurposeEnum.SIGNUP)
        .userHandle(userHandle)
        .expiresAt(expiresAt)
        .build();
  }
}
