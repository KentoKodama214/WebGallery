package com.web.gallery.infrastructure.persistence.repository.inquiry;

import com.web.gallery.application.repository.inquiry.InquiryReplyMstRepository;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.inquiry.InquiryId;
import com.web.gallery.domain.model.inquiry.ReplyNo;
import com.web.gallery.infrastructure.persistence.entity.inquiry.InquiryReplyMstCondition;
import com.web.gallery.infrastructure.persistence.mapper.inquiry.InquiryReplyMstMapper;
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

  /**
   * 指定アカウントが管理者として、他ユーザーのお問い合わせへ投稿した返信が存在するかどうかを判定する
   *
   * @param adminAccountNo 返信した管理者のアカウント番号
   * @return 1件以上存在する場合、true
   */
  @Override
  public boolean existsReplyToOthersInquiry(AccountNo adminAccountNo) {
    return inquiryReplyMstMapper.exists(
        InquiryReplyMstCondition.byAdminAccountNoExcludingOwnInquiry(adminAccountNo.value()));
  }
}
