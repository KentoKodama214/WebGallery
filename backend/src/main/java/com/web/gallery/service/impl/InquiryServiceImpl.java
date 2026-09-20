package com.web.gallery.service.impl;

import com.web.gallery.aggregate.Inquiry;
import com.web.gallery.config.InquiryConfig;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryId;
import com.web.gallery.domain.inquiry.InquiryNo;
import com.web.gallery.domain.inquiry.ReplyBody;
import com.web.gallery.domain.inquiry.ReplyNo;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.enumeration.InquiryStatusEnum;
import com.web.gallery.event.InquiryRegisteredEvent;
import com.web.gallery.event.InquiryRepliedEvent;
import com.web.gallery.event.InquiryWithdrawnEvent;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.model.inquiry.InquiryDetailModel;
import com.web.gallery.model.inquiry.InquiryGetModel;
import com.web.gallery.model.inquiry.InquiryListGetModel;
import com.web.gallery.model.inquiry.InquiryPageModel;
import com.web.gallery.repository.InquiryAggregateRepository;
import com.web.gallery.repository.InquiryMstRepository;
import com.web.gallery.repository.InquiryReplyMstRepository;
import com.web.gallery.service.InquiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** お問い合わせに関するビジネスロジックを扱うServiceの実装クラス */
@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {

  private final InquiryMstRepository inquiryMstRepository;
  private final InquiryReplyMstRepository inquiryReplyMstRepository;
  private final InquiryAggregateRepository inquiryAggregateRepository;
  private final InquiryConfig inquiryConfig;
  private final ApplicationEventPublisher applicationEventPublisher;

  /**
   * お問い合わせを新規登録する
   *
   * @param requestDetail 登録リクエストのお問い合わせ詳細情報
   * @return 新規採番したお問い合わせ番号
   * @throws GalleryException 登録に失敗した場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public InquiryNo registInquiry(InquiryDetailModel requestDetail) throws GalleryException {
    InquiryNo newInquiryNo = inquiryMstRepository.getNewInquiryNo(requestDetail.getAccountNo());
    Inquiry inquiry = Inquiry.forRegist(requestDetail, newInquiryNo);

    inquiryAggregateRepository.regist(inquiry);

    applicationEventPublisher.publishEvent(
        new InquiryRegisteredEvent(inquiry.getAccountNo(), inquiry.getInquiryNo()));

    return inquiry.getInquiryNo();
  }

  /**
   * 自分のお問い合わせ一覧を取得する
   *
   * @param inquiryListGetModel {@link InquiryListGetModel}
   * @return {@link InquiryPageModel}
   */
  @Override
  @Transactional(readOnly = true)
  public InquiryPageModel getInquiryList(InquiryListGetModel inquiryListGetModel) {
    InquiryGetModel inquiryGetModel =
        InquiryGetModel.of(inquiryListGetModel, inquiryConfig.getInquiryCountPerPage());
    return inquiryMstRepository.getInquiryList(inquiryGetModel);
  }

  /**
   * 自分のお問い合わせの詳細情報（返信を含む）を取得する
   *
   * <p>未読の返信が存在する場合、取得と同時に既読化する
   *
   * @param accountNo アカウント番号
   * @param inquiryNo お問い合わせ番号
   * @return {@link InquiryDetailModel}
   * @throws GalleryException お問い合わせが存在しなかった場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public InquiryDetailModel getInquiryDetail(AccountNo accountNo, InquiryNo inquiryNo)
      throws GalleryException {
    InquiryDetailModel detail = inquiryMstRepository.getInquiryDetail(accountNo, inquiryNo);

    if (Boolean.FALSE.equals(detail.getIsReadByUser())) {
      Inquiry inquiry = Inquiry.reconstruct(detail, detail.getReplyModelList());
      inquiry.markReadByUser();
      inquiryAggregateRepository.markReadByUser(inquiry);
      return inquiry.getDetail();
    }

    return detail;
  }

  /**
   * お問い合わせ一覧を取得する（管理者用、全アカウントが対象）
   *
   * @param inquiryListGetModel {@link InquiryListGetModel}
   * @return {@link InquiryPageModel}
   */
  @Override
  @Transactional(readOnly = true)
  public InquiryPageModel getInquiryListForAdmin(InquiryListGetModel inquiryListGetModel) {
    InquiryGetModel inquiryGetModel =
        InquiryGetModel.of(inquiryListGetModel, inquiryConfig.getInquiryCountPerPage());
    return inquiryMstRepository.getInquiryListForAdmin(inquiryGetModel);
  }

  /**
   * お問い合わせの詳細情報（返信を含む）を取得する（管理者用）
   *
   * @param inquiryId ID
   * @return {@link InquiryDetailModel}
   * @throws GalleryException お問い合わせが存在しなかった場合
   */
  @Override
  @Transactional(readOnly = true)
  public InquiryDetailModel getInquiryDetailForAdmin(InquiryId inquiryId) throws GalleryException {
    return inquiryMstRepository.getInquiryDetailForAdmin(inquiryId);
  }

  /**
   * お問い合わせに返信する（管理者用）
   *
   * @param inquiryId ID
   * @param adminAccountNo 返信する管理者のアカウント番号
   * @param body 返信本文
   * @return 新規採番した返信番号
   * @throws GalleryException 以下のいずれかに該当する場合 ・お問い合わせが存在しない場合 ・取り下げられている場合 ・返信の登録に失敗した場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public ReplyNo replyToInquiry(InquiryId inquiryId, AccountNo adminAccountNo, ReplyBody body)
      throws GalleryException {
    InquiryDetailModel detail = inquiryMstRepository.getInquiryDetailForAdmin(inquiryId);

    if (detail.getStatusKbn() == InquiryStatusEnum.WITHDRAWN) {
      throw ErrorEnum.CANNOT_REPLY_TO_WITHDRAWN_INQUIRY.toException();
    }

    Inquiry inquiry = Inquiry.reconstruct(detail, detail.getReplyModelList());

    ReplyNo newReplyNo = inquiryReplyMstRepository.getNewReplyNo(inquiryId);
    inquiry.addReply(adminAccountNo, body, newReplyNo);

    inquiryAggregateRepository.addReply(inquiry);

    applicationEventPublisher.publishEvent(
        new InquiryRepliedEvent(inquiry.getAccountNo(), inquiry.getInquiryNo(), newReplyNo));

    return newReplyNo;
  }

  /**
   * 自分のお問い合わせを取り下げる
   *
   * @param accountNo アカウント番号
   * @param inquiryNo お問い合わせ番号
   * @throws GalleryException 以下のいずれかに該当する場合 ・お問い合わせが存在しない場合 ・取り下げに失敗した場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public void withdrawInquiry(AccountNo accountNo, InquiryNo inquiryNo) throws GalleryException {
    InquiryDetailModel detail = inquiryMstRepository.getInquiryDetail(accountNo, inquiryNo);
    Inquiry inquiry = Inquiry.reconstruct(detail, detail.getReplyModelList());

    inquiry.withdraw();
    inquiryAggregateRepository.withdraw(inquiry);

    applicationEventPublisher.publishEvent(
        new InquiryWithdrawnEvent(inquiry.getAccountNo(), inquiry.getInquiryNo()));
  }
}
