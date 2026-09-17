package com.web.gallery.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** お問い合わせ一覧の1ページ分の情報を受け渡すためのModelクラス */
@Value
@Builder
public class InquiryPageModel {
  /** お問い合わせ一覧 */
  @NonNull private InquiryModelList inquiryModelList;

  /** 最後のページかどうか */
  @NonNull private Boolean isLast;

  /**
   * InquiryModelListと最後のページかどうかからInquiryPageModelを生成する
   *
   * @param inquiryModelList {@link InquiryModelList}
   * @param isLast 最後のページかどうか
   * @return {@link InquiryPageModel}
   */
  public static InquiryPageModel of(InquiryModelList inquiryModelList, Boolean isLast) {
    return InquiryPageModel.builder().inquiryModelList(inquiryModelList).isLast(isLast).build();
  }
}
