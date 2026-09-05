package com.web.gallery.controller.response;

import com.web.gallery.exception.GalleryException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

/** エラー発生時のレスポンスパラメータを保持するクラス */
@Schema(description = "エラーレスポンス")
@Data
@Builder
public class ErrorResponse {
  /** HTTPステータス */
  @Schema(description = "HTTPステータスコード", example = "400")
  private Integer httpStatus;

  /** エラーコード */
  @Schema(description = "エラーコード", example = "E-C-0001")
  private String errorCode;

  /** エラーメッセージ */
  @Schema(description = "エラーメッセージ", example = "入力内容が不正です")
  private String errorMessage;

  /**
   * GalleryExceptionからエラーレスポンスを生成する
   *
   * @param exception {@link GalleryException}
   * @param httpStatus HTTPステータス
   * @return {@link ErrorResponse}
   */
  public static ErrorResponse of(GalleryException exception, HttpStatus httpStatus) {
    return ErrorResponse.builder()
        .httpStatus(httpStatus.value())
        .errorCode(exception.getErrorCode())
        .errorMessage(exception.getMessage())
        .build();
  }
}
