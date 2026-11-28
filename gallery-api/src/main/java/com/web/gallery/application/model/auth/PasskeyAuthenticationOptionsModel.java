package com.web.gallery.application.model.auth;

import com.web.gallery.domain.model.auth.CredentialId;
import com.web.gallery.domain.model.auth.PasskeyChallenge;
import com.web.gallery.domain.model.auth.PasskeyTimeoutMillis;
import com.web.gallery.domain.model.auth.RelyingPartyId;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * パスキー認証の開始時にクライアントへ渡すオプション（PublicKeyCredentialRequestOptions）を受け渡すためのModelクラス
 *
 * <p>ログインではアカウントを特定しない（discoverable credential）ためallowCredentialsは空とし、
 * 重要操作の前の本人確認（再認証）では本人のパスキーに限定する
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@Builder
public class PasskeyAuthenticationOptionsModel {
  /** チャレンジ */
  @NonNull private PasskeyChallenge challenge;

  /** RP ID */
  @NonNull private RelyingPartyId rpId;

  /** 許可するクレデンシャルID（allowCredentials。ログインでは空、本人確認では本人のパスキー） */
  @NonNull private List<CredentialId> allowCredentialIds;

  /** タイムアウト（ミリ秒） */
  @NonNull private PasskeyTimeoutMillis timeoutMillis;
}
