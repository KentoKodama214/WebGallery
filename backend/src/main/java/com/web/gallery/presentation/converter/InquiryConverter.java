package com.web.gallery.presentation.converter;

import com.web.gallery.application.model.inquiry.InquiryDetailModel;
import com.web.gallery.application.model.inquiry.InquiryListGetModel;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.inquiry.InquiryBody;
import com.web.gallery.domain.model.inquiry.InquirySubject;
import com.web.gallery.presentation.request.inquiry.AdminInquiryListRequest;
import com.web.gallery.presentation.request.inquiry.InquiryListRequest;
import com.web.gallery.presentation.request.inquiry.InquiryRegistRequest;
import org.springframework.stereotype.Component;

/** お問い合わせ関連のRequestをModelへ変換するConverterクラス */
@Component
public class InquiryConverter {

  /**
   * 新規お問い合わせ登録リクエストとログイン中のアカウント番号からInquiryDetailModelを生成する
   *
   * <p>アカウント番号はリクエストボディではなくセッションから取得した値を用いる（他人になりすましたお問い合わせ登録を防ぐため）
   *
   * @param request {@link InquiryRegistRequest}
   * @param accountNo ログイン中のアカウント番号
   * @return {@link InquiryDetailModel}
   */
  public InquiryDetailModel toInquiryDetailModel(
      InquiryRegistRequest request, AccountNo accountNo) {
    return InquiryDetailModel.builder()
        .accountNo(accountNo)
        .subject(new InquirySubject(request.getSubject()))
        .body(new InquiryBody(request.getBody()))
        .build();
  }

  /**
   * 自分のお問い合わせ一覧リクエストとログイン中のアカウント番号からInquiryListGetModelを生成する
   *
   * @param request {@link InquiryListRequest}
   * @param accountNo ログイン中のアカウント番号
   * @return {@link InquiryListGetModel}
   */
  public InquiryListGetModel toInquiryListGetModel(
      InquiryListRequest request, AccountNo accountNo) {
    return InquiryListGetModel.builder().accountNo(accountNo).pageNo(request.getPageNo()).build();
  }

  /**
   * 管理者用お問い合わせ一覧リクエストからInquiryListGetModelを生成する
   *
   * @param request {@link AdminInquiryListRequest}
   * @return {@link InquiryListGetModel}
   */
  public InquiryListGetModel toInquiryListGetModel(AdminInquiryListRequest request) {
    return InquiryListGetModel.builder()
        .statusKbn(request.getStatusKbn())
        .pageNo(request.getPageNo())
        .build();
  }
}
