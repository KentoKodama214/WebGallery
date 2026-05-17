package com.web.gallery.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ログイン認証時のリクエストパラメータを保持するクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
@Data
public class AuthLoginRequest {
	/** アカウントID */
	@NotBlank(message = "{validation.common.notBlank}")
	private String accountId;

	/** パスワード */
	@NotBlank(message = "{validation.common.notBlank}")
	private String password;
}
