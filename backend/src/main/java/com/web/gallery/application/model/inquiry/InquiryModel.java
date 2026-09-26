package com.web.gallery.application.model.inquiry;

import com.web.gallery.domain.enumeration.InquiryStatusEnum;
import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountName;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.inquiry.InquiryId;
import com.web.gallery.domain.model.inquiry.InquiryNo;
import com.web.gallery.domain.model.inquiry.InquirySubject;
import com.web.gallery.dto.InquiryDto;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * お問い合わせ一覧の1件分の情報を受け渡すためのModelクラス
 *
 * <p>{@code accountId}・{@code accountName}は管理者用の全件一覧でのみ設定され、自分の一覧取得ではnullとなる
 */
@Value
@Builder
public class InquiryModel {
  /** ID */
  @NonNull private InquiryId inquiryId;

  /** アカウント番号 */
  @NonNull private AccountNo accountNo;

  /** アカウントID（管理者用の全件一覧でのみ設定） */
  private AccountId accountId;

  /** アカウント名（管理者用の全件一覧でのみ設定） */
  private AccountName accountName;

  /** お問い合わせ番号 */
  @NonNull private InquiryNo inquiryNo;

  /** 件名 */
  @NonNull private InquirySubject subject;

  /**
   * ステータス区分
   *
   * <p>{@link InquiryStatusEnum}
   */
  @NonNull private InquiryStatusEnum statusKbn;

  /** ユーザー既読フラグ */
  @NonNull private Boolean isReadByUser;

  /** 作成日時 */
  @NonNull private OffsetDateTime createdAt;

  /**
   * InquiryDtoからInquiryModelを生成する
   *
   * @param dto {@link InquiryDto}
   * @return {@link InquiryModel}
   */
  public static InquiryModel from(InquiryDto dto) {
    return InquiryModel.builder()
        .inquiryId(new InquiryId(dto.getId()))
        .accountNo(new AccountNo(dto.getAccountNo()))
        .accountId(dto.getAccountId() != null ? new AccountId(dto.getAccountId()) : null)
        .accountName(dto.getAccountName() != null ? new AccountName(dto.getAccountName()) : null)
        .inquiryNo(new InquiryNo(dto.getInquiryNo()))
        .subject(new InquirySubject(dto.getSubject()))
        .statusKbn(dto.getStatusKbn())
        .isReadByUser(dto.getIsReadByUser())
        .createdAt(dto.getCreatedAt())
        .build();
  }
}
