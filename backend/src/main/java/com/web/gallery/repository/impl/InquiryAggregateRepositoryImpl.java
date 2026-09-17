package com.web.gallery.repository.impl;

import com.web.gallery.aggregate.Inquiry;
import com.web.gallery.entity.InquiryMst;
import com.web.gallery.entity.InquiryMstCondition;
import com.web.gallery.entity.InquiryMstUpdateTarget;
import com.web.gallery.entity.InquiryReplyMst;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.mapper.InquiryMstMapper;
import com.web.gallery.mapper.InquiryReplyMstMapper;
import com.web.gallery.repository.InquiryAggregateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * お問い合わせ集約（{@link Inquiry}）を永続化するRepositoryの実装クラス
 *
 * <p>InquiryMst・InquiryReplyMstの2テーブルへの永続化を、お問い合わせの登録・返信追加・既読化という
 * ユースケース単位で整合性のある1操作としてまとめる。他のRepositoryには依存せず、Mapperを直接操作する
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class InquiryAggregateRepositoryImpl implements InquiryAggregateRepository {

  private final InquiryMstMapper inquiryMstMapper;
  private final InquiryReplyMstMapper inquiryReplyMstMapper;

  /**
   * お問い合わせ集約を新規登録する
   *
   * @param inquiry {@link Inquiry}
   * @throws GalleryException 登録に失敗した場合
   */
  @Override
  public void regist(Inquiry inquiry) throws GalleryException {
    InquiryMst inquiryMst =
        InquiryMst.fromForRegist(inquiry.getDetail(), inquiry.getInquiryNo().value());

    if (inquiryMstMapper.insert(inquiryMst) < 1) {
      log.warn(
          "InquiryMst: Regist Failed (AccountNo: {}, InquiryNo: {})",
          inquiry.getAccountNo().value(),
          inquiry.getInquiryNo().value());
      throw ErrorEnum.FAIL_TO_REGIST_INQUIRY.toException();
    }
  }

  /**
   * お問い合わせに返信を追加し、あわせてお問い合わせ本体のステータス・ユーザー既読フラグを更新する
   *
   * @param inquiry {@link Inquiry}
   * @throws GalleryException 更新に失敗した場合
   */
  @Override
  public void addReply(Inquiry inquiry) throws GalleryException {
    InquiryReplyMst inquiryReplyMst = InquiryReplyMst.fromForRegist(inquiry.getNewReply());
    if (inquiryReplyMstMapper.insert(inquiryReplyMst) < 1) {
      log.warn(
          "InquiryReplyMst: Regist Failed (InquiryId: {}, ReplyNo: {})",
          inquiry.getInquiryId().value(),
          inquiry.getNewReply().getReplyNo().value());
      throw ErrorEnum.FAIL_TO_REPLY_INQUIRY.toException();
    }

    InquiryMstCondition condition = InquiryMstCondition.byId(inquiry.getInquiryId().value());
    InquiryMstUpdateTarget target =
        InquiryMstUpdateTarget.forReply(
            inquiry.getDetail(), inquiry.getNewReply().getAdminAccountNo().value());

    if (inquiryMstMapper.update(condition, target) < 1) {
      log.warn(
          "InquiryMst: Update Failed for reply (InquiryId: {})", inquiry.getInquiryId().value());
      throw ErrorEnum.FAIL_TO_REPLY_INQUIRY.toException();
    }
  }

  /**
   * お問い合わせのユーザー既読フラグを更新する
   *
   * @param inquiry {@link Inquiry}
   * @throws GalleryException 更新に失敗した場合
   */
  @Override
  public void markReadByUser(Inquiry inquiry) throws GalleryException {
    InquiryMstCondition condition =
        InquiryMstCondition.byAccountAndInquiryNo(
            inquiry.getAccountNo().value(), inquiry.getInquiryNo().value());
    InquiryMstUpdateTarget target =
        InquiryMstUpdateTarget.forMarkRead(inquiry.getDetail(), inquiry.getAccountNo().value());

    if (inquiryMstMapper.update(condition, target) < 1) {
      log.warn(
          "InquiryMst: Update Failed for markReadByUser (AccountNo: {}, InquiryNo: {})",
          inquiry.getAccountNo().value(),
          inquiry.getInquiryNo().value());
      throw ErrorEnum.INQUIRY_NOT_FOUND.toException();
    }
  }
}
