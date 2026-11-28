package com.web.gallery.application.model.auth;

import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountName;
import com.web.gallery.domain.model.auth.CredentialId;
import com.web.gallery.domain.model.auth.PasskeyChallenge;
import com.web.gallery.domain.model.auth.PasskeyTimeoutMillis;
import com.web.gallery.domain.model.auth.PasskeyUserHandle;
import com.web.gallery.domain.model.auth.RelyingPartyId;
import com.web.gallery.domain.model.auth.RelyingPartyName;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * パスキー登録の開始時にクライアントへ渡すオプション（PublicKeyCredentialCreationOptions）を受け渡すためのModelクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@Builder
public class PasskeyRegistrationOptionsModel {
  /** チャレンジ */
  @NonNull private PasskeyChallenge challenge;

  /** RP ID */
  @NonNull private RelyingPartyId rpId;

  /** RP名 */
  @NonNull private RelyingPartyName rpName;

  /** ユーザーハンドル（WebAuthnのuser.id） */
  @NonNull private PasskeyUserHandle userHandle;

  /** アカウントID（WebAuthnのuser.name。パスキーマネージャーでの表示用） */
  @NonNull private AccountId accountId;

  /** アカウント名（WebAuthnのuser.displayName。パスキーマネージャーでの表示用） */
  @NonNull private AccountName accountName;

  /** 登録済みのクレデンシャルID（excludeCredentials。同じ認証器への二重登録を防ぐ） */
  @NonNull private List<CredentialId> excludeCredentialIds;

  /** タイムアウト（ミリ秒） */
  @NonNull private PasskeyTimeoutMillis timeoutMillis;
}
