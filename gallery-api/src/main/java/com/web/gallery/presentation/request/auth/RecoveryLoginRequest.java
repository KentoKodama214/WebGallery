package com.web.gallery.presentation.request.auth;

import com.web.gallery.domain.constant.Consts;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 復旧ログイン時のリクエストパラメータを保持するクラス */
@Schema(description = "復旧ログインリクエスト")
@Data
public class RecoveryLoginRequest {
  /**
   * 登録レスポンス
   *
   * <p>復旧オプション発行APIで受け取ったオプションで{@code navigator.credentials.create()}が返した {@code
   * PublicKeyCredential}を{@code toJSON()}した値を、さらにJSON文字列にしたもの
   */
  @Schema(description = "登録レスポンス（PublicKeyCredential#toJSON()のJSON文字列）")
  @NotBlank(message = "{validation.common.notBlank}")
  @Size(max = Consts.PASSKEY_RESPONSE_JSON_MAX_LENGTH, message = "{validation.common.max_length}")
  private String credential;

  /** パスキー名 */
  @Schema(description = "パスキー名", example = "新しいiPhone")
  @NotBlank(message = "{validation.common.notBlank}")
  @Size(max = Consts.PASSKEY_NAME_MAX_LENGTH, message = "{validation.common.max_length}")
  private String passkeyName;
}
