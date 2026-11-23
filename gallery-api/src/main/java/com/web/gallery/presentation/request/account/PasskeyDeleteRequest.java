package com.web.gallery.presentation.request.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** パスキー削除時のリクエストパラメータを保持するクラス */
@Schema(description = "パスキー削除リクエスト")
@Data
public class PasskeyDeleteRequest {
  /**
   * 現在のパスワード
   *
   * <p>本人確認（再認証）に用いる。ログイン手段を失わせる操作のため必須。
   */
  @Schema(description = "現在のパスワード", example = "password01")
  @NotBlank(message = "{validation.common.notBlank}")
  @Size(max = 72, message = "{validation.common.max_length}")
  private String currentPassword;
}
