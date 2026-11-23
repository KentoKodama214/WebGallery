package com.web.gallery.application.model.auth;

import com.web.gallery.domain.model.auth.PasskeyChallenge;
import com.web.gallery.domain.model.auth.PasskeyTimeoutMillis;
import com.web.gallery.domain.model.auth.RelyingPartyId;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * パスキー認証の開始時にクライアントへ渡すオプション（PublicKeyCredentialRequestOptions）を受け渡すためのModelクラス
 *
 * <p>アカウントを特定しない（discoverable credential）ため、allowCredentialsは持たない
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

  /** タイムアウト（ミリ秒） */
  @NonNull private PasskeyTimeoutMillis timeoutMillis;
}
