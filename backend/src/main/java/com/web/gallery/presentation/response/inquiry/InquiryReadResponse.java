package com.web.gallery.presentation.response.inquiry;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

/** お問い合わせ既読化のレスポンスパラメータを保持するクラス */
@Schema(description = "お問い合わせ既読化レスポンス")
@Data
@Builder
public class InquiryReadResponse {
  /** HTTPステータス */
  @Schema(description = "HTTPステータスコード", example = "200")
  private Integer httpStatus;

  /** 既読化成功 */
  @Schema(description = "成功", example = "true")
  private Boolean isSuccess;

  /** メッセージ */
  @Schema(description = "メッセージ")
  private String message;

  /**
   * 成功レスポンスを生成する
   *
   * @param message メッセージ
   * @return {@link InquiryReadResponse}
   */
  public static InquiryReadResponse of(String message) {
    return InquiryReadResponse.builder()
        .httpStatus(HttpStatus.OK.value())
        .isSuccess(true)
        .message(message)
        .build();
  }
}
