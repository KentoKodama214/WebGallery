package com.web.gallery.application.service.inquiry;

import com.web.gallery.application.model.inquiry.InquiryDetailModel;
import com.web.gallery.application.model.inquiry.InquiryListGetModel;
import com.web.gallery.application.model.inquiry.InquiryPageModel;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.inquiry.InquiryId;
import com.web.gallery.domain.model.inquiry.InquiryNo;
import com.web.gallery.domain.model.inquiry.ReplyBody;
import com.web.gallery.domain.model.inquiry.ReplyNo;

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
   * <p>副作用を持たない参照専用の処理とし、既読化は{@link #markInquiryAsRead}で明示的に行う
   *
   * @param accountNo アカウント番号
   * @param inquiryNo お問い合わせ番号
   * @return {@link InquiryDetailModel}
   * @throws GalleryException お問い合わせが存在しなかった場合
   */
  InquiryDetailModel getInquiryDetail(AccountNo accountNo, InquiryNo inquiryNo)
      throws GalleryException;

  /**
   * 自分のお問い合わせを既読にする
   *
   * <p>既に既読の場合は何もしない（何度呼び出しても結果が変わらない冪等な操作）
   *
   * @param accountNo アカウント番号
   * @param inquiryNo お問い合わせ番号
   * @throws GalleryException 以下のいずれかに該当する場合 ・お問い合わせが存在しない場合 ・既読化に失敗した場合
   */
  void markInquiryAsRead(AccountNo accountNo, InquiryNo inquiryNo) throws GalleryException;

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

  /**
   * 自分のお問い合わせを取り下げる
   *
   * @param accountNo アカウント番号
   * @param inquiryNo お問い合わせ番号
   * @throws GalleryException 以下のいずれかに該当する場合 ・お問い合わせが存在しない場合 ・取り下げに失敗した場合
   */
  void withdrawInquiry(AccountNo accountNo, InquiryNo inquiryNo) throws GalleryException;
}
