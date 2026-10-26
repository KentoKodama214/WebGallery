package com.web.gallery.infrastructure.persistence.entity.inquiry;

import lombok.Builder;
import lombok.Data;

/** お問い合わせ返信マスタテーブルの抽出条件クラス */
@Data
@Builder
public class InquiryReplyMstCondition {
  /** お問い合わせID */
  private Long inquiryId;

  /** 返信した管理者のアカウント番号 */
  private Long adminAccountNo;

  /** お問い合わせの登録者（問い合わせた本人）のアカウント番号 */
  private Long inquiryAccountNo;

  /** 抽出対象から除外する、お問い合わせの登録者（問い合わせた本人）のアカウント番号 */
  private Long excludingInquiryAccountNo;

  /**
   * お問い合わせIDによる抽出条件を生成する
   *
   * @param inquiryId お問い合わせID
   * @return {@link InquiryReplyMstCondition}
   */
  public static InquiryReplyMstCondition byInquiryId(Long inquiryId) {
    return InquiryReplyMstCondition.builder().inquiryId(inquiryId).build();
  }

  /**
   * 「指定アカウントが管理者として、自分以外が起票したお問い合わせへ投稿した返信」による抽出条件を生成する
   *
   * <p>アカウント削除の可否判定に使用する。自分が起票したお問い合わせに紐づく返信は、退会時に {@code
   * byInquiryAccountNo}の条件でスレッドごと削除されるため、削除をブロックする理由 （無関係な第三者のスレッドから回答本文だけが消える）に該当せず、判定対象から除外する
   *
   * @param adminAccountNo 返信した管理者のアカウント番号
   * @return {@link InquiryReplyMstCondition}
   */
  public static InquiryReplyMstCondition byAdminAccountNoExcludingOwnInquiry(Long adminAccountNo) {
    return InquiryReplyMstCondition.builder()
        .adminAccountNo(adminAccountNo)
        .excludingInquiryAccountNo(adminAccountNo)
        .build();
  }

  /**
   * お問い合わせの登録者のアカウント番号による抽出条件を生成する
   *
   * <p>アカウント削除時に、そのアカウントが登録したお問い合わせに紐づく返信をまとめて削除するために使用する
   *
   * @param inquiryAccountNo お問い合わせの登録者のアカウント番号
   * @return {@link InquiryReplyMstCondition}
   */
  public static InquiryReplyMstCondition byInquiryAccountNo(Long inquiryAccountNo) {
    return InquiryReplyMstCondition.builder().inquiryAccountNo(inquiryAccountNo).build();
  }
}
