package com.web.gallery.presentation.request.auth;

import com.web.gallery.domain.constant.Consts;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** パスキーログイン時のリクエストパラメータを保持するクラス */
@Schema(description = "パスキーログインリクエスト")
@Data
public class PasskeyLoginRequest {
  /**
   * 認証レスポンス
   *
   * <p>{@code navigator.credentials.get()}が返した{@code PublicKeyCredential}を{@code toJSON()}した値を、
   * さらにJSON文字列にしたもの（サーバ側で長さを検証してから解釈するため文字列で受け取る）
   */
  @Schema(description = "認証レスポンス（PublicKeyCredential#toJSON()のJSON文字列）")
  @NotBlank(message = "{validation.common.notBlank}")
  @Size(max = Consts.PASSKEY_RESPONSE_JSON_MAX_LENGTH, message = "{validation.common.max_length}")
  private String credential;
}
