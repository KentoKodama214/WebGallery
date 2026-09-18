package com.web.gallery.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

/** お問い合わせ登録のレスポンスパラメータを保持するクラス */
@Schema(description = "お問い合わせ登録レスポンス")
@Data
@Builder
public class InquiryRegistResponse {
  /** HTTPステータス */
  @Schema(description = "HTTPステータスコード", example = "200")
  private Integer httpStatus;

  /** 登録成功 */
  @Schema(description = "成功", example = "true")
  private Boolean isSuccess;

  /** メッセージ */
  @Schema(description = "メッセージ")
  private String message;

  /** お問い合わせ番号 */
  @Schema(description = "お問い合わせ番号")
  private Long inquiryNo;

  /**
   * 成功レスポンスを生成する
   *
   * @param message メッセージ
   * @param inquiryNo お問い合わせ番号
   * @return {@link InquiryRegistResponse}
   */
  public static InquiryRegistResponse of(String message, Long inquiryNo) {
    return InquiryRegistResponse.builder()
        .httpStatus(HttpStatus.OK.value())
        .isSuccess(true)
        .message(message)
        .inquiryNo(inquiryNo)
        .build();
  }
}
