package com.web.gallery.service;

import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.Password;
import com.web.gallery.domain.auth.RefreshTokenValue;
import com.web.gallery.model.AuthTokenModel;

/**
 * JWT認証に関するサービスのインターフェース
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
public interface AuthService {
	/**
	 * ログイン認証を行い、トークンを発行する
	 *
	 * @param	accountId	アカウントID
	 * @param	password	パスワード
	 * @return				{@link AuthTokenModel}
	 */
	AuthTokenModel login(AccountId accountId, Password password);

	/**
	 * リフレッシュトークンを検証し、新しいアクセストークンを発行する
	 *
	 * @param	refreshToken	リフレッシュトークン
	 * @return					{@link AuthTokenModel}
	 */
	AuthTokenModel refresh(RefreshTokenValue refreshToken);

	/**
	 * ログアウトし、リフレッシュトークンを無効化する
	 *
	 * @param	refreshToken	リフレッシュトークン
	 */
	void logout(RefreshTokenValue refreshToken);

	/**
	 * 有効期限切れのリフレッシュトークンをDBから削除する<p>
	 * 無効化済み・期限切れのレコードが蓄積してテーブルが肥大化するのを防ぐため、定期的に実行される
	 */
	void purgeExpiredRefreshTokens();
}
