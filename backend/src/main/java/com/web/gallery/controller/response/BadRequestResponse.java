package com.web.gallery.controller.response;

import com.web.gallery.exception.BadRequestException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

/** パラメータ不正の時のレスポンスパラメータを保持するクラス */
@Schema(description = "バリデーションエラーレスポンス")
@Data
@Builder
public class BadRequestResponse {
  /** HTTPステータス */
  @Schema(description = "HTTPステータスコード", example = "400")
  private Integer httpStatus;

  /** 登録成功 */
  @Schema(description = "成功", example = "false")
  private Boolean isSuccess;

  /** メッセージ */
  @Schema(description = "エラーメッセージ")
  private String message;

  /**
   * BadRequestExceptionからエラーレスポンスを生成する
   *
   * @param exception {@link BadRequestException}
   * @return {@link BadRequestResponse}
   */
  public static BadRequestResponse of(BadRequestException exception) {
    return BadRequestResponse.builder()
        .httpStatus(HttpStatus.BAD_REQUEST.value())
        .isSuccess(false)
        .message(exception.getMessage())
        .build();
  }
}
