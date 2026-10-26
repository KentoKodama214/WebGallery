package com.web.gallery.application.model.inquiry;

import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.inquiry.InquiryId;
import com.web.gallery.domain.model.inquiry.ReplyBody;
import com.web.gallery.domain.model.inquiry.ReplyNo;
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
}
