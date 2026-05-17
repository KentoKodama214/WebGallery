package com.web.gallery.model;

import lombok.Builder;
import lombok.Data;

/**
 * 認証トークン情報を保持するモデルクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
@Data
@Builder
public class AuthTokenModel {
	/** アクセストークン */
	private String accessToken;

	/** リフレッシュトークン */
	private String refreshToken;

	/** アクセストークン有効期限（秒） */
	private Long expiresIn;
}
