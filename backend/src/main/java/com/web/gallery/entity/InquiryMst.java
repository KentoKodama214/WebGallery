package com.web.gallery.entity;

import com.web.gallery.enumeration.InquiryStatusEnum;
import com.web.gallery.model.InquiryDetailModel;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

/** お問い合わせマスタテーブルのEntityクラス */
@Data
@Builder
public class InquiryMst {
  /** ID */
  private Long id;

  /** アカウント番号 */
  private Long accountNo;

  /** お問い合わせ番号 */
  private Long inquiryNo;

  /** 作成者 */
  private Long createdBy;

  /** 作成日時 */
  private OffsetDateTime createdAt;

  /** 更新者 */
  private Long updatedBy;

  /** 更新日時 */
  private OffsetDateTime updatedAt;

  /** 件名 */
  private String subject;

  /** 本文 */
  private String body;

  /**
   * ステータス区分
   *
   * <p>{@link InquiryStatusEnum}
   */
  private InquiryStatusEnum statusKbn;

  /** ユーザー既読フラグ */
  private Boolean isReadByUser;

  /**
   * 新規登録用のInquiryDetailModelからInquiryMstエンティティを生成する
   *
   * @param model {@link InquiryDetailModel}
   * @param newInquiryNo 新規採番したお問い合わせ番号
   * @return {@link InquiryMst}
   */
  public static InquiryMst fromForRegist(InquiryDetailModel model, Long newInquiryNo) {
    return InquiryMst.builder()
        .accountNo(model.getAccountNo().value())
        .inquiryNo(newInquiryNo)
        .createdBy(model.getAccountNo().value())
        .updatedBy(model.getAccountNo().value())
        .subject(model.getSubject().value())
        .body(model.getBody().value())
        .statusKbn(InquiryStatusEnum.UNREPLIED)
        .isReadByUser(true)
        .build();
  }
}
