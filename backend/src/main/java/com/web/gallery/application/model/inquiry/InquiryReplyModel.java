package com.web.gallery.application.model.inquiry;

import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.inquiry.InquiryId;
import com.web.gallery.domain.model.inquiry.ReplyBody;
import com.web.gallery.domain.model.inquiry.ReplyNo;
import com.web.gallery.infrastructure.persistence.entity.inquiry.InquiryReplyMst;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** お問い合わせ返信の情報を受け渡すためのModelクラス */
@Value
@Builder
public class InquiryReplyModel {
  /** お問い合わせID */
  @NonNull private InquiryId inquiryId;

  /** 返信番号 */
  @NonNull private ReplyNo replyNo;

  /** 返信した管理者のアカウント番号 */
  @NonNull private AccountNo adminAccountNo;

  /** 返信本文 */
  @NonNull private ReplyBody body;

  /** 作成日時 */
  private OffsetDateTime createdAt;

  /**
   * InquiryReplyMstエンティティからInquiryReplyModelを生成する
   *
   * @param entity {@link InquiryReplyMst}
   * @return {@link InquiryReplyModel}
   */
  public static InquiryReplyModel from(InquiryReplyMst entity) {
    return InquiryReplyModel.builder()
        .inquiryId(new InquiryId(entity.getInquiryId()))
        .replyNo(new ReplyNo(entity.getReplyNo()))
        .adminAccountNo(new AccountNo(entity.getAdminAccountNo()))
        .body(new ReplyBody(entity.getBody()))
        .createdAt(entity.getCreatedAt())
        .build();
  }
}
