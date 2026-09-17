package com.web.gallery.controller.response;

import com.web.gallery.enumeration.InquiryStatusEnum;
import com.web.gallery.model.InquiryModel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

/** 自分のお問い合わせ一覧の1件分のレスポンスパラメータを保持するクラス */
@Schema(description = "お問い合わせ一覧アイテムレスポンス")
@Data
@Builder
public class InquiryListItemResponse {
  /** お問い合わせ番号 */
  @Schema(description = "お問い合わせ番号", example = "1")
  private Long inquiryNo;

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

  /** ユーザー既読フラグ */
  @Schema(description = "ユーザー既読フラグ")
  private Boolean isReadByUser;

  /** 作成日時 */
  @Schema(description = "作成日時")
  private OffsetDateTime createdAt;

  /**
   * InquiryModelからInquiryListItemResponseを生成する
   *
   * @param model {@link InquiryModel}
   * @return {@link InquiryListItemResponse}
   */
  public static InquiryListItemResponse from(InquiryModel model) {
    return InquiryListItemResponse.builder()
        .inquiryNo(model.getInquiryNo().value())
        .subject(model.getSubject().value())
        .statusKbn(model.getStatusKbn())
        .isReadByUser(model.getIsReadByUser())
        .createdAt(model.getCreatedAt())
        .build();
  }
}
