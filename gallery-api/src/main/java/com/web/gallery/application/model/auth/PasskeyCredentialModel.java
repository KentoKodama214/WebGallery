package com.web.gallery.application.model.auth;

import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.auth.AttestedCredentialData;
import com.web.gallery.domain.model.auth.CredentialId;
import com.web.gallery.domain.model.auth.IsBackedUp;
import com.web.gallery.domain.model.auth.IsBackupEligible;
import com.web.gallery.domain.model.auth.IsUvInitialized;
import com.web.gallery.domain.model.auth.LastUsedAt;
import com.web.gallery.domain.model.auth.PasskeyName;
import com.web.gallery.domain.model.auth.PasskeyNo;
import com.web.gallery.domain.model.auth.PasskeyTransports;
import com.web.gallery.domain.model.auth.SignCount;
import com.web.gallery.domain.model.common.CreatedAt;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * パスキー（WebAuthnクレデンシャル）を受け渡すためのModelクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@Builder(toBuilder = true)
public class PasskeyCredentialModel {
  /** パスキー番号（{@link #forRegist}生成時は採番前のためnull許容） */
  private PasskeyNo passkeyNo;

  /** アカウント番号 */
  @NonNull private AccountNo accountNo;

  /** クレデンシャルID */
  @NonNull private CredentialId credentialId;

  /** 認証器データ（公開鍵を含む） */
  @NonNull private AttestedCredentialData attestedCredentialData;

  /** 署名カウンタ */
  @NonNull private SignCount signCount;

  /** 認証器の通信手段 */
  @NonNull private PasskeyTransports transports;

  /** バックアップ可能フラグ */
  @NonNull private IsBackupEligible isBackupEligible;

  /** バックアップ済みフラグ */
  @NonNull private IsBackedUp isBackedUp;

  /** ユーザー検証済みフラグ */
  @NonNull private IsUvInitialized isUvInitialized;

  /** パスキー名 */
  @NonNull private PasskeyName passkeyName;

  /** 最終使用日時（{@link #forRegist}生成時はDB側の既定値を使うためnull許容） */
  private LastUsedAt lastUsedAt;

  /** 作成日時（{@link #forRegist}生成時はDB側の既定値を使うためnull許容） */
  private CreatedAt createdAt;

  /**
   * 検証済みの登録レスポンスから、新規登録用のPasskeyCredentialModelを生成する
   *
   * @param accountNo アカウント番号
   * @param attestation 検証済みの登録レスポンス {@link PasskeyAttestationModel}
   * @param passkeyName パスキー名
   * @return {@link PasskeyCredentialModel}
   */
  public static PasskeyCredentialModel forRegist(
      AccountNo accountNo, PasskeyAttestationModel attestation, PasskeyName passkeyName) {
    return PasskeyCredentialModel.builder()
        .accountNo(accountNo)
        .credentialId(attestation.getCredentialId())
        .attestedCredentialData(attestation.getAttestedCredentialData())
        .signCount(attestation.getSignCount())
        .transports(attestation.getTransports())
        .isBackupEligible(attestation.getIsBackupEligible())
        .isBackedUp(attestation.getIsBackedUp())
        .isUvInitialized(attestation.getIsUvInitialized())
        .passkeyName(passkeyName)
        .build();
  }

  /**
   * 認証に成功した結果（署名カウンタ・バックアップ状態）を反映したPasskeyCredentialModelを生成する
   *
   * @param assertion 検証済みの認証レスポンス {@link PasskeyAssertionResultModel}
   * @param lastUsedAt 最終使用日時（認証した日時）
   * @return {@link PasskeyCredentialModel}
   */
  public PasskeyCredentialModel withAuthenticationResult(
      PasskeyAssertionResultModel assertion, LastUsedAt lastUsedAt) {
    return this.toBuilder()
        .signCount(assertion.getSignCount())
        .isBackedUp(assertion.getIsBackedUp())
        .lastUsedAt(lastUsedAt)
        .build();
  }
}
