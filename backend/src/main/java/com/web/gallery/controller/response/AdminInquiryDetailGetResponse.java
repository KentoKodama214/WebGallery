package com.web.gallery.controller.response;

import com.web.gallery.enumeration.InquiryStatusEnum;
import com.web.gallery.model.InquiryDetailModel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** 管理者用お問い合わせ詳細のレスポンスパラメータを保持するクラス */
@Schema(description = "管理者用お問い合わせ詳細レスポンス")
@Data
@Builder
public class AdminInquiryDetailGetResponse {
  /** ID */
  @Schema(description = "ID", example = "1")
  private Long inquiryId;

  /** アカウントID */
  @Schema(description = "アカウントID")
  private String accountId;

  /** アカウント名 */
  @Schema(description = "アカウント名")
  private String accountName;

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
   * InquiryDetailModelからAdminInquiryDetailGetResponseを生成する
   *
   * @param model {@link InquiryDetailModel}
   * @return {@link AdminInquiryDetailGetResponse}
   */
  public static AdminInquiryDetailGetResponse from(InquiryDetailModel model) {
    List<InquiryReplyItemResponse> replyItemResponseList =
        model.getReplyModelList().stream().map(InquiryReplyItemResponse::from).toList();

    return AdminInquiryDetailGetResponse.builder()
        .inquiryId(model.getInquiryId().value())
        .accountId(model.getAccountId() != null ? model.getAccountId().value() : null)
        .accountName(model.getAccountName() != null ? model.getAccountName().value() : null)
        .subject(model.getSubject().value())
        .body(model.getBody().value())
        .statusKbn(model.getStatusKbn())
        .createdAt(model.getCreatedAt())
        .replyList(replyItemResponseList)
        .build();
  }
}
