package com.web.gallary.controller.response;

import lombok.Builder;
import lombok.Data;

/**
 * ログイン認証のレスポンスパラメータを保持するクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
@Data
@Builder
public class AuthLoginResponse {
	/** アクセストークン */
	private String accessToken;

	/** アクセストークン有効期限（秒） */
	private Long expiresIn;
}
