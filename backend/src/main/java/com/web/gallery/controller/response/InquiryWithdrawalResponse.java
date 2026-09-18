package com.web.gallery.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

/** お問い合わせ取り下げのレスポンスパラメータを保持するクラス */
@Schema(description = "お問い合わせ取り下げレスポンス")
@Data
@Builder
public class InquiryWithdrawalResponse {
  /** HTTPステータス */
  @Schema(description = "HTTPステータスコード", example = "200")
  private Integer httpStatus;

  /** 登録成功 */
  @Schema(description = "成功", example = "true")
  private Boolean isSuccess;

  /** メッセージ */
  @Schema(description = "メッセージ")
  private String message;

  /**
   * 成功レスポンスを生成する
   *
   * @param message メッセージ
   * @return {@link InquiryWithdrawalResponse}
   */
  public static InquiryWithdrawalResponse of(String message) {
    return InquiryWithdrawalResponse.builder()
        .httpStatus(HttpStatus.OK.value())
        .isSuccess(true)
        .message(message)
        .build();
  }
}
