package com.web.gallary.controller.response;

import com.web.gallary.model.AuthTokenModel;

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

	/**
	 * AuthTokenModelからAuthLoginResponseを生成する
	 *
	 * @param	model	{@link AuthTokenModel}
	 * @return			{@link AuthLoginResponse}
	 */
	public static AuthLoginResponse from(AuthTokenModel model) {
		return AuthLoginResponse.builder()
				.accessToken(model.getAccessToken())
				.expiresIn(model.getExpiresIn())
				.build();
	}
}
