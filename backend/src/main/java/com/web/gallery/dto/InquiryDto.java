package com.web.gallery.dto;

import com.web.gallery.enumeration.InquiryStatusEnum;
import java.time.OffsetDateTime;
import lombok.Data;

/**
 * お問い合わせ一覧を取得するためのDtoクラス
 *
 * <p>ユーザー向け一覧では{@code accountId}・{@code accountName}は取得しない（null）。管理者向け一覧では {@code
 * common.account}と結合してこれらを取得する
 */
@Data
public class InquiryDto {
  /** ID */
  private Long id;

  /** アカウント番号 */
  private Long accountNo;

  /** アカウントID */
  private String accountId;

  /** アカウント名 */
  private String accountName;

  /** お問い合わせ番号 */
  private Long inquiryNo;

  /** 件名 */
  private String subject;

  /**
   * ステータス区分
   *
   * <p>{@link InquiryStatusEnum}
   */
  private InquiryStatusEnum statusKbn;

  /** ユーザー既読フラグ */
  private Boolean isReadByUser;

  /** 作成日時 */
  private OffsetDateTime createdAt;
}
