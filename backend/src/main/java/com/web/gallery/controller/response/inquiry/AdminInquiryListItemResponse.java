package com.web.gallery.controller.response.inquiry;

import com.web.gallery.enumeration.InquiryStatusEnum;
import com.web.gallery.model.inquiry.InquiryModel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

/** 管理者用お問い合わせ一覧の1件分のレスポンスパラメータを保持するクラス */
@Schema(description = "管理者用お問い合わせ一覧アイテムレスポンス")
@Data
@Builder
public class AdminInquiryListItemResponse {
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

  /**
   * InquiryModelからAdminInquiryListItemResponseを生成する
   *
   * @param model {@link InquiryModel}
   * @return {@link AdminInquiryListItemResponse}
   */
  public static AdminInquiryListItemResponse from(InquiryModel model) {
    return AdminInquiryListItemResponse.builder()
        .inquiryId(model.getInquiryId().value())
        .accountId(model.getAccountId() != null ? model.getAccountId().value() : null)
        .accountName(model.getAccountName() != null ? model.getAccountName().value() : null)
        .subject(model.getSubject().value())
        .statusKbn(model.getStatusKbn())
        .createdAt(model.getCreatedAt())
        .build();
  }
}
