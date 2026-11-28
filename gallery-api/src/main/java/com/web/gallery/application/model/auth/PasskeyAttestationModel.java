package com.web.gallery.application.model.auth;

import com.web.gallery.domain.model.auth.AttestedCredentialData;
import com.web.gallery.domain.model.auth.CredentialId;
import com.web.gallery.domain.model.auth.IsBackedUp;
import com.web.gallery.domain.model.auth.IsBackupEligible;
import com.web.gallery.domain.model.auth.IsUvInitialized;
import com.web.gallery.domain.model.auth.PasskeyTransports;
import com.web.gallery.domain.model.auth.SignCount;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * 検証済みのパスキー登録レスポンス（attestation）から取り出した値を受け渡すためのModelクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@Builder
public class PasskeyAttestationModel {
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
}
