package com.web.gallery.controller.response;

import com.web.gallery.model.InquiryReplyModel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

/** お問い合わせ返信の1件分のレスポンスパラメータを保持するクラス */
@Schema(description = "お問い合わせ返信アイテムレスポンス")
@Data
@Builder
public class InquiryReplyItemResponse {
  /** 返信番号 */
  @Schema(description = "返信番号", example = "1")
  private Long replyNo;

  /** 返信本文 */
  @Schema(description = "返信本文")
  private String body;

  /** 作成日時 */
  @Schema(description = "作成日時")
  private OffsetDateTime createdAt;

  /**
   * InquiryReplyModelからInquiryReplyItemResponseを生成する
   *
   * @param model {@link InquiryReplyModel}
   * @return {@link InquiryReplyItemResponse}
   */
  public static InquiryReplyItemResponse from(InquiryReplyModel model) {
    return InquiryReplyItemResponse.builder()
        .replyNo(model.getReplyNo().value())
        .body(model.getBody().value())
        .createdAt(model.getCreatedAt())
        .build();
  }
}
