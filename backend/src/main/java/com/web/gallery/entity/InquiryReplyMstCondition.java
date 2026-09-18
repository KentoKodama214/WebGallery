package com.web.gallery.entity;

import lombok.Builder;
import lombok.Data;

/** お問い合わせ返信マスタテーブルの抽出条件クラス */
@Data
@Builder
public class InquiryReplyMstCondition {
  /** お問い合わせID */
  private Long inquiryId;

  /**
   * お問い合わせIDによる抽出条件を生成する
   *
   * @param inquiryId お問い合わせID
   * @return {@link InquiryReplyMstCondition}
   */
  public static InquiryReplyMstCondition byInquiryId(Long inquiryId) {
    return InquiryReplyMstCondition.builder().inquiryId(inquiryId).build();
  }
}
