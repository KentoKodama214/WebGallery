package com.web.gallery.repository;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryId;
import com.web.gallery.domain.inquiry.InquiryNo;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.model.inquiry.InquiryDetailModel;
import com.web.gallery.model.inquiry.InquiryGetModel;
import com.web.gallery.model.inquiry.InquiryPageModel;

/** お問い合わせマスタデータを永続化するRepositoryクラス */
public interface InquiryMstRepository {
  /**
   * アカウント番号から新しいお問い合わせ番号を発番する
   *
   * @param accountNo アカウント番号
   * @return 新規採番したお問い合わせ番号
   */
  InquiryNo getNewInquiryNo(AccountNo accountNo);

  /**
   * 自分のお問い合わせ一覧を、ページング情報に従い取得する
   *
   * @param inquiryGetModel {@link InquiryGetModel}
   * @return {@link InquiryPageModel}
   */
  InquiryPageModel getInquiryList(InquiryGetModel inquiryGetModel);

  /**
   * お問い合わせ一覧を、ページング情報に従い取得する（管理者用、全アカウントが対象）
   *
   * @param inquiryGetModel {@link InquiryGetModel}
   * @return {@link InquiryPageModel}
   */
  InquiryPageModel getInquiryListForAdmin(InquiryGetModel inquiryGetModel);

  /**
   * 自分のお問い合わせの詳細情報（返信を含む）を取得する
   *
   * @param accountNo アカウント番号
   * @param inquiryNo お問い合わせ番号
   * @return {@link InquiryDetailModel}
   * @throws GalleryException お問い合わせが存在しなかった場合
   */
  InquiryDetailModel getInquiryDetail(AccountNo accountNo, InquiryNo inquiryNo)
      throws GalleryException;

  /**
   * お問い合わせの詳細情報（返信を含む）を取得する（管理者用）
   *
   * @param inquiryId ID
   * @return {@link InquiryDetailModel}
   * @throws GalleryException お問い合わせが存在しなかった場合
   */
  InquiryDetailModel getInquiryDetailForAdmin(InquiryId inquiryId) throws GalleryException;
}
