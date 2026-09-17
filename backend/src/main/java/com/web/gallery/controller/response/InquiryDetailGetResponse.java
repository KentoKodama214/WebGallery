package com.web.gallery.controller.response;

import com.web.gallery.enumeration.InquiryStatusEnum;
import com.web.gallery.model.InquiryDetailModel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** 自分のお問い合わせ詳細のレスポンスパラメータを保持するクラス */
@Schema(description = "お問い合わせ詳細レスポンス")
@Data
@Builder
public class InquiryDetailGetResponse {
  /** お問い合わせ番号 */
  @Schema(description = "お問い合わせ番号", example = "1")
  private Long inquiryNo;

  /** 件名 */
  @Schema(description = "件名")
  private String subject;

  /** 本文 */
  @Schema(description = "本文")
  private String body;

  /**
   * ステータス区分
   *
   * <p>{@link InquiryStatusEnum}
   */
  @Schema(description = "ステータス区分")
  private InquiryStatusEnum statusKbn;

  /** 作成日時 */
  @Schema(description = "作成日時")
  private OffsetDateTime createdAt;

  /** 返信一覧 */
  @Schema(description = "返信一覧")
  private List<InquiryReplyItemResponse> replyList;

  /**
   * InquiryDetailModelからInquiryDetailGetResponseを生成する
   *
   * @param model {@link InquiryDetailModel}
   * @return {@link InquiryDetailGetResponse}
   */
  public static InquiryDetailGetResponse from(InquiryDetailModel model) {
    List<InquiryReplyItemResponse> replyItemResponseList =
        model.getReplyModelList().stream().map(InquiryReplyItemResponse::from).toList();

    return InquiryDetailGetResponse.builder()
        .inquiryNo(model.getInquiryNo().value())
        .subject(model.getSubject().value())
        .body(model.getBody().value())
        .statusKbn(model.getStatusKbn())
        .createdAt(model.getCreatedAt())
        .replyList(replyItemResponseList)
        .build();
  }
}
