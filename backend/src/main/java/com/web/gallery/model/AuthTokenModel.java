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
}
