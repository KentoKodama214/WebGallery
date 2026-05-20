package com.web.gallery.service;

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
	AuthTokenModel login(String accountId, String password);

	/**
	 * リフレッシュトークンを検証し、新しいアクセストークンを発行する
	 *
	 * @param	refreshToken	リフレッシュトークン
	 * @return					{@link AuthTokenModel}
	 */
	AuthTokenModel refresh(String refreshToken);

	/**
	 * ログアウトし、リフレッシュトークンを無効化する
	 *
	 * @param	refreshToken	リフレッシュトークン
	 */
	void logout(String refreshToken);
}
