package com.web.gallery.model;

import com.web.gallery.domain.auth.AccessToken;
import com.web.gallery.domain.auth.ExpiresIn;
import com.web.gallery.domain.auth.RefreshTokenValue;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * 認証トークン情報を保持するモデルクラス
 */
@Value
@Builder
public class AuthTokenModel {
	/** アクセストークン */
	@NonNull
	private AccessToken accessToken;

	/** リフレッシュトークン */
	@NonNull
	private RefreshTokenValue refreshToken;

	/** アクセストークン有効期限（秒） */
	@NonNull
	private ExpiresIn expiresIn;

	/**
	 * アクセストークン・リフレッシュトークン・有効期限（秒）からAuthTokenModelを生成する
	 *
	 * @param	accessToken			アクセストークン文字列
	 * @param	refreshToken		リフレッシュトークン文字列
	 * @param	expiresInSeconds	有効期限（秒）
	 * @return						{@link AuthTokenModel}
	 */
	public static AuthTokenModel of(String accessToken, String refreshToken, Long expiresInSeconds) {
		return AuthTokenModel.builder()
				.accessToken(new AccessToken(accessToken))
				.refreshToken(new RefreshTokenValue(refreshToken))
				.expiresIn(new ExpiresIn(expiresInSeconds))
				.build();
	}
}
