package com.web.gallery.model.inquiry;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.enumeration.InquiryStatusEnum;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** お問い合わせの一覧をDBから取得する際の抽出・ページング情報を受け渡すためのModelクラス */
@Value
@Builder
public class InquiryGetModel {
  /** アカウント番号（自分の一覧取得の場合のみ設定。管理者用の全件一覧取得の場合はnull） */
  private AccountNo accountNo;

  /**
   * ステータス区分による絞り込み（任意）
   *
   * <p>{@link InquiryStatusEnum}
   */
  private InquiryStatusEnum statusKbn;

  /** 取得件数上限（最後のページかどうかの判定用に、1ページあたりの表示件数より1件多く取得する） */
  @NonNull private Integer limit;

  /** 取得開始位置（0始まり） */
  @NonNull private Integer offset;

  /**
   * InquiryListGetModelと1ページあたりの表示件数からInquiryGetModelを生成する
   *
   * @param inquiryListGetModel {@link InquiryListGetModel}
   * @param inquiryCountPerPage 1ページあたりの表示件数
   * @return {@link InquiryGetModel}
   */
  public static InquiryGetModel of(
      InquiryListGetModel inquiryListGetModel, Integer inquiryCountPerPage) {
    return InquiryGetModel.builder()
        .accountNo(inquiryListGetModel.getAccountNo())
        .statusKbn(inquiryListGetModel.getStatusKbn())
        .limit(inquiryCountPerPage + 1)
        .offset((inquiryListGetModel.getPageNo() - 1) * inquiryCountPerPage)
        .build();
  }
}
