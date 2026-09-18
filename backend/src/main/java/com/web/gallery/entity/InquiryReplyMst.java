package com.web.gallery.entity;

import com.web.gallery.model.InquiryReplyModel;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

/** お問い合わせ返信マスタテーブルのEntityクラス */
@Data
@Builder
public class InquiryReplyMst {
  /** ID */
  private Long id;

  /** お問い合わせID */
  private Long inquiryId;

  /** 返信番号 */
  private Long replyNo;

  /** 返信した管理者のアカウント番号 */
  private Long adminAccountNo;

  /** 作成者 */
  private Long createdBy;

  /** 作成日時 */
  private OffsetDateTime createdAt;

  /** 返信本文 */
  private String body;

  /**
   * 新規登録用のInquiryReplyModelからInquiryReplyMstエンティティを生成する
   *
   * @param model {@link InquiryReplyModel}
   * @return {@link InquiryReplyMst}
   */
  public static InquiryReplyMst fromForRegist(InquiryReplyModel model) {
    return InquiryReplyMst.builder()
        .inquiryId(model.getInquiryId().value())
        .replyNo(model.getReplyNo().value())
        .adminAccountNo(model.getAdminAccountNo().value())
        .createdBy(model.getAdminAccountNo().value())
        .body(model.getBody().value())
        .build();
  }
}
