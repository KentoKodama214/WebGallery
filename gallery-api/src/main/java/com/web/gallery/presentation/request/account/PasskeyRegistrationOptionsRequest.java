package com.web.gallery.presentation.request.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** パスキー登録オプション発行時のリクエストパラメータを保持するクラス */
@Schema(description = "パスキー登録オプション発行リクエスト")
@Data
public class PasskeyRegistrationOptionsRequest {
  /**
   * 現在のパスワード
   *
   * <p>本人確認（再認証）に用いる。アクセストークンを盗んだ攻撃者が自分のパスキーを登録して居座るのを防ぐため必須。
   */
  @Schema(description = "現在のパスワード", example = "password01")
  @NotBlank(message = "{validation.common.notBlank}")
  @Size(max = 72, message = "{validation.common.max_length}")
  private String currentPassword;
}
