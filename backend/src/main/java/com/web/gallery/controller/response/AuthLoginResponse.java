package com.web.gallery.controller.response;

import com.web.gallery.model.AuthTokenModel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * ログイン認証のレスポンスパラメータを保持するクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
@Schema(description = "ログイン認証レスポンス")
@Data
@Builder
public class AuthLoginResponse {
	/** アクセストークン */
	@Schema(description = "アクセストークン")
	private String accessToken;

	/** アクセストークン有効期限（秒） */
	@Schema(description = "アクセストークン有効期限（秒）", example = "900")
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
