package com.web.gallery.application.model.inquiry;

import com.web.gallery.domain.enumeration.InquiryStatusEnum;
import com.web.gallery.domain.model.account.AccountNo;
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
}
