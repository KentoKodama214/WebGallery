package com.web.gallery.application.service.auth;

import com.web.gallery.application.model.auth.AuthTokenModel;
import com.web.gallery.application.model.auth.PasskeyAuthenticationOptionsModel;
import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.Password;
import com.web.gallery.domain.model.auth.PasskeyResponseJson;
import com.web.gallery.domain.model.auth.RefreshTokenValue;
import com.web.gallery.domain.model.common.IpAddress;

/**
 * JWT認証に関するサービスのインターフェース
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
public interface AuthService {
  /**
   * ログイン認証を行い、トークンを発行する
   *
   * @param accountId アカウントID
   * @param password パスワード
   * @param ipAddress 送信元IPアドレス（ログイン履歴に記録するため、認証結果に伝播させる）
   * @return {@link AuthTokenModel}
   */
  AuthTokenModel login(AccountId accountId, Password password, IpAddress ipAddress);

  /**
   * パスキー認証の開始時に、クライアントへ渡すオプション（チャレンジ等）を発行する
   *
   * @return {@link PasskeyAuthenticationOptionsModel}
   */
  PasskeyAuthenticationOptionsModel createPasskeyAuthenticationOptions();

  /**
   * パスキーの認証レスポンスを検証し、トークンを発行する
   *
   * @param authenticationResponseJson 認証レスポンス（{@code PublicKeyCredential#toJSON()}の形式）
   * @param ipAddress 送信元IPアドレス（ログイン履歴に記録する）
   * @return {@link AuthTokenModel}
   */
  AuthTokenModel loginWithPasskey(
      PasskeyResponseJson authenticationResponseJson, IpAddress ipAddress);

  /**
   * リフレッシュトークンを検証し、新しいアクセストークンを発行する
   *
   * @param refreshToken リフレッシュトークン
   * @return {@link AuthTokenModel}
   */
  AuthTokenModel refresh(RefreshTokenValue refreshToken);

  /**
   * ログアウトし、リフレッシュトークンを無効化する
   *
   * @param refreshToken リフレッシュトークン
   */
  void logout(RefreshTokenValue refreshToken);

  /**
   * 有効期限切れのリフレッシュトークンをDBから削除する
   *
   * <p>無効化済み・期限切れのレコードが蓄積してテーブルが肥大化するのを防ぐため、定期的に実行される
   */
  void purgeExpiredRefreshTokens();

  /**
   * 有効期限切れのWebAuthnチャレンジをDBから削除する
   *
   * <p>発行されたまま使われなかったチャレンジが蓄積してテーブルが肥大化するのを防ぐため、定期的に実行される
   */
  void purgeExpiredWebAuthnChallenges();
}
