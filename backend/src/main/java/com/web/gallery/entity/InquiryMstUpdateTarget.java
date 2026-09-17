package com.web.gallery.entity;

import com.web.gallery.enumeration.InquiryStatusEnum;
import com.web.gallery.model.InquiryDetailModel;
import lombok.Builder;
import lombok.Data;

/** お問い合わせマスタテーブルの更新対象クラス */
@Data
@Builder
public class InquiryMstUpdateTarget {
  /** 更新者 */
  private Long updatedBy;

  /**
   * ステータス区分
   *
   * <p>{@link InquiryStatusEnum}
   */
  private InquiryStatusEnum statusKbn;

  /** ユーザー既読フラグ */
  private Boolean isReadByUser;

  /**
   * 返信登録に伴う更新対象を生成する
   *
   * <p>ステータス・ユーザー既読フラグの遷移先は{@link com.web.gallery.aggregate.Inquiry#addReply}が 決定済みの{@link
   * InquiryDetailModel}からそのまま読み取る（ビジネスルールを集約に一元化するため）
   *
   * @param detail 返信追加後の{@link InquiryDetailModel}
   * @param adminAccountNo 返信した管理者のアカウント番号
   * @return {@link InquiryMstUpdateTarget}
   */
  public static InquiryMstUpdateTarget forReply(InquiryDetailModel detail, Long adminAccountNo) {
    return InquiryMstUpdateTarget.builder()
        .updatedBy(adminAccountNo)
        .statusKbn(detail.getStatusKbn())
        .isReadByUser(detail.getIsReadByUser())
        .build();
  }

  /**
   * ユーザー既読化に伴う更新対象を生成する
   *
   * @param detail 既読化後の{@link InquiryDetailModel}
   * @param accountNo 既読化したユーザーのアカウント番号
   * @return {@link InquiryMstUpdateTarget}
   */
  public static InquiryMstUpdateTarget forMarkRead(InquiryDetailModel detail, Long accountNo) {
    return InquiryMstUpdateTarget.builder()
        .updatedBy(accountNo)
        .isReadByUser(detail.getIsReadByUser())
        .build();
  }
}
