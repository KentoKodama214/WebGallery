package com.web.gallery.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 認証エラー発生時のレスポンスパラメータを保持するクラス
 *
 * @param message エラーメッセージ
 */
@Schema(description = "認証エラーレスポンス")
public record AuthErrorResponse(
    @Schema(description = "エラーメッセージ", example = "アカウントIDまたはパスワードが間違っています。") String message) {

  /**
   * エラーメッセージからエラーレスポンスを生成する
   *
   * @param message エラーメッセージ
   * @return {@link AuthErrorResponse}
   */
  public static AuthErrorResponse of(String message) {
    return new AuthErrorResponse(message);
  }
}
