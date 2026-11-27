package com.web.gallery.presentation.request.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * アカウント新規登録オプション発行時のリクエストパラメータを保持するクラス
 *
 * <p>アカウントID・アカウント名は、パスキーマネージャーでの表示（WebAuthnのuser.name・user.displayName）に使う
 */
@Schema(description = "アカウント新規登録オプション発行リクエスト")
@Data
public class AccountSignupOptionsRequest {
  /**
   * アカウントID
   *
   * <p>半角英数8〜20桁、ブランクなし
   */
  @Schema(description = "アカウントID（半角英数8〜20桁）", example = "testuser01")
  @NotBlank(message = "{validation.common.notBlank}")
  @Size(min = 8, max = 20, message = "{validation.common.min_max_length}")
  @Pattern(regexp = "[a-zA-Z0-9]{8,20}", message = "{validation.common.pattern}")
  private String accountId;

  /**
   * アカウント名
   *
   * <p>ブランクなし
   */
  @Schema(description = "アカウント名", example = "テストユーザー")
  @NotBlank(message = "{validation.common.notBlank}")
  @Size(max = 50, message = "{validation.common.max_length}")
  @Pattern(regexp = "[^　]+", message = "{validation.common.all_space}")
  private String accountName;
}
