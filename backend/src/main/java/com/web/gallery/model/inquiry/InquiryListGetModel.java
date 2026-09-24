package com.web.gallery.model.inquiry;

import com.web.gallery.controller.request.inquiry.AdminInquiryListRequest;
import com.web.gallery.controller.request.inquiry.InquiryListRequest;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.enumeration.InquiryStatusEnum;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** お問い合わせの一覧を取得するために必要な情報を受け渡すためのModelクラス */
@Value
@Builder
public class InquiryListGetModel {
  /** アカウント番号（自分の一覧取得の場合のみ設定。管理者用の全件一覧取得の場合はnull） */
  private AccountNo accountNo;

  /**
   * ステータス区分による絞り込み（任意）
   *
   * <p>{@link InquiryStatusEnum}
   */
  private InquiryStatusEnum statusKbn;

  /** ページ番号 */
  @NonNull private Integer pageNo;

  /**
   * 自分のお問い合わせ一覧リクエストとログイン中のアカウント番号からInquiryListGetModelを生成する
   *
   * @param request {@link InquiryListRequest}
   * @param accountNo ログイン中のアカウント番号
   * @return {@link InquiryListGetModel}
   */
  public static InquiryListGetModel from(InquiryListRequest request, AccountNo accountNo) {
    return InquiryListGetModel.builder().accountNo(accountNo).pageNo(request.getPageNo()).build();
  }

  /**
   * 管理者用お問い合わせ一覧リクエストからInquiryListGetModelを生成する
   *
   * @param request {@link AdminInquiryListRequest}
   * @return {@link InquiryListGetModel}
   */
  public static InquiryListGetModel from(AdminInquiryListRequest request) {
    return InquiryListGetModel.builder()
        .statusKbn(request.getStatusKbn())
        .pageNo(request.getPageNo())
        .build();
  }
}
