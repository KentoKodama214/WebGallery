package com.web.gallery.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ログイン認証時のリクエストパラメータを保持するクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
@Schema(description = "ログイン認証リクエスト")
@Data
public class AuthLoginRequest {
	/** アカウントID */
	@Schema(description = "アカウントID", example = "testuser01")
	@NotBlank(message = "{validation.common.notBlank}")
	private String accountId;

	/** パスワード */
	@Schema(description = "パスワード", example = "password01")
	@NotBlank(message = "{validation.common.notBlank}")
	private String password;
}
