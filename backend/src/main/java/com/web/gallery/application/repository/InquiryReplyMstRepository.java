package com.web.gallery.application.repository;

import com.web.gallery.domain.model.inquiry.InquiryId;
import com.web.gallery.domain.model.inquiry.ReplyNo;

/** お問い合わせ返信マスタデータを永続化するRepositoryクラス */
public interface InquiryReplyMstRepository {
  /**
   * お問い合わせIDから新しい返信番号を発番する
   *
   * @param inquiryId ID
   * @return 新規採番した返信番号
   */
  ReplyNo getNewReplyNo(InquiryId inquiryId);
}
