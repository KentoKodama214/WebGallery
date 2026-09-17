package com.web.gallery.repository.impl;

import com.web.gallery.domain.inquiry.InquiryId;
import com.web.gallery.domain.inquiry.ReplyNo;
import com.web.gallery.mapper.InquiryReplyMstMapper;
import com.web.gallery.repository.InquiryReplyMstRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/** お問い合わせ返信マスタデータを永続化するRepositoryの実装クラス */
@Repository
@RequiredArgsConstructor
public class InquiryReplyMstRepositoryImpl implements InquiryReplyMstRepository {

  private final InquiryReplyMstMapper inquiryReplyMstMapper;

  /**
   * お問い合わせIDから新しい返信番号を発番する
   *
   * @param inquiryId ID
   * @return 新規採番した返信番号
   */
  @Override
  public ReplyNo getNewReplyNo(InquiryId inquiryId) {
    Long maxReplyNo = inquiryReplyMstMapper.getMaxReplyNo(inquiryId.value());
    return ReplyNo.next(maxReplyNo);
  }
}
