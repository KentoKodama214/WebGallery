package com.web.gallery.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * アカウント削除時のリクエストパラメータを保持するクラス
 */
@Schema(description = "アカウント削除リクエスト")
@Data
public class AccountDeleteRequest {
	/**
	 * 現在のパスワード
	 * <p>
	 * 本人確認（再認証）に用いる。誤操作・アクセストークン漏洩時の被害を限定するため必須。
	 * アクセスログ等に記録されやすいカスタムヘッダーではなく、POSTのリクエストボディで受け取る。
	 */
	@Schema(description = "現在のパスワード", example = "password01")
	@NotBlank(message = "{validation.common.notBlank}")
	@Size(max = 72, message = "{validation.common.max_length}")
	private String currentPassword;
}
