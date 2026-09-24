package com.web.gallery.controller.response.inquiry;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

/** お問い合わせ返信登録のレスポンスパラメータを保持するクラス */
@Schema(description = "お問い合わせ返信レスポンス")
@Data
@Builder
public class InquiryReplyResponse {
  /** HTTPステータス */
  @Schema(description = "HTTPステータスコード", example = "200")
  private Integer httpStatus;

  /** 登録成功 */
  @Schema(description = "成功", example = "true")
  private Boolean isSuccess;

  /** メッセージ */
  @Schema(description = "メッセージ")
  private String message;

  /** 返信番号 */
  @Schema(description = "返信番号")
  private Long replyNo;

  /**
   * 成功レスポンスを生成する
   *
   * @param message メッセージ
   * @param replyNo 返信番号
   * @return {@link InquiryReplyResponse}
   */
  public static InquiryReplyResponse of(String message, Long replyNo) {
    return InquiryReplyResponse.builder()
        .httpStatus(HttpStatus.OK.value())
        .isSuccess(true)
        .message(message)
        .replyNo(replyNo)
        .build();
  }
}
