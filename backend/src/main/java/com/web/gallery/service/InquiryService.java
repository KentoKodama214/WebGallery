package com.web.gallery.service;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryId;
import com.web.gallery.domain.inquiry.InquiryNo;
import com.web.gallery.domain.inquiry.ReplyBody;
import com.web.gallery.domain.inquiry.ReplyNo;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.model.InquiryDetailModel;
import com.web.gallery.model.InquiryListGetModel;
import com.web.gallery.model.InquiryPageModel;

/** お問い合わせに関するビジネスロジックを扱うServiceインターフェース */
public interface InquiryService {
  /**
   * お問い合わせを新規登録する
   *
   * @param requestDetail 登録リクエストのお問い合わせ詳細情報
   * @return 新規採番したお問い合わせ番号
   * @throws GalleryException 登録に失敗した場合
   */
  InquiryNo registInquiry(InquiryDetailModel requestDetail) throws GalleryException;

  /**
   * 自分のお問い合わせ一覧を取得する
   *
   * @param inquiryListGetModel {@link InquiryListGetModel}
   * @return {@link InquiryPageModel}
   */
  InquiryPageModel getInquiryList(InquiryListGetModel inquiryListGetModel);

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
  InquiryDetailModel getInquiryDetail(AccountNo accountNo, InquiryNo inquiryNo)
      throws GalleryException;

  /**
   * お問い合わせ一覧を取得する（管理者用、全アカウントが対象）
   *
   * @param inquiryListGetModel {@link InquiryListGetModel}
   * @return {@link InquiryPageModel}
   */
  InquiryPageModel getInquiryListForAdmin(InquiryListGetModel inquiryListGetModel);

  /**
   * お問い合わせの詳細情報（返信を含む）を取得する（管理者用）
   *
   * @param inquiryId ID
   * @return {@link InquiryDetailModel}
   * @throws GalleryException お問い合わせが存在しなかった場合
   */
  InquiryDetailModel getInquiryDetailForAdmin(InquiryId inquiryId) throws GalleryException;

  /**
   * お問い合わせに返信する（管理者用）
   *
   * @param inquiryId ID
   * @param adminAccountNo 返信する管理者のアカウント番号
   * @param body 返信本文
   * @return 新規採番した返信番号
   * @throws GalleryException 以下のいずれかに該当する場合 ・お問い合わせが存在しない場合 ・返信の登録に失敗した場合
   */
  ReplyNo replyToInquiry(InquiryId inquiryId, AccountNo adminAccountNo, ReplyBody body)
      throws GalleryException;
}
