package com.web.gallery.model;

import com.web.gallery.controller.request.InquiryRegistRequest;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryBody;
import com.web.gallery.domain.inquiry.InquiryId;
import com.web.gallery.domain.inquiry.InquiryNo;
import com.web.gallery.domain.inquiry.InquirySubject;
import com.web.gallery.dto.InquiryDetailDto;
import com.web.gallery.entity.InquiryReplyMst;
import com.web.gallery.enumeration.InquiryStatusEnum;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * お問い合わせの詳細情報（返信を含む）を受け渡すためのModelクラス
 *
 * <p>{@code accountId}・{@code accountName}は管理者用の詳細取得でのみ設定され、自分の詳細取得ではnullとなる。 {@code
 * inquiryId}・{@code inquiryNo}・{@code statusKbn}・{@code isReadByUser}・{@code createdAt}・ {@code
 * replyModelList}は新規登録リクエストからの生成時は未設定（null）となる
 */
@Value
@Builder(toBuilder = true)
public class InquiryDetailModel {
  /** アカウント番号 */
  @NonNull private AccountNo accountNo;

  /** アカウントID（管理者用の詳細取得でのみ設定） */
  private AccountId accountId;

  /** アカウント名（管理者用の詳細取得でのみ設定） */
  private AccountName accountName;

  /** ID */
  private InquiryId inquiryId;

  /** お問い合わせ番号 */
  private InquiryNo inquiryNo;

  /** 件名 */
  @NonNull private InquirySubject subject;

  /** 本文 */
  @NonNull private InquiryBody body;

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

  /** 返信一覧 */
  private InquiryReplyModelList replyModelList;

  /**
   * InquiryDetailDtoと返信エンティティリストからInquiryDetailModelを生成する
   *
   * @param dto {@link InquiryDetailDto}
   * @param replyMstList 該当お問い合わせの返信エンティティリスト
   * @return {@link InquiryDetailModel}
   */
  public static InquiryDetailModel from(InquiryDetailDto dto, List<InquiryReplyMst> replyMstList) {
    return InquiryDetailModel.builder()
        .accountNo(new AccountNo(dto.getAccountNo()))
        .accountId(dto.getAccountId() != null ? new AccountId(dto.getAccountId()) : null)
        .accountName(dto.getAccountName() != null ? new AccountName(dto.getAccountName()) : null)
        .inquiryId(new InquiryId(dto.getId()))
        .inquiryNo(new InquiryNo(dto.getInquiryNo()))
        .subject(new InquirySubject(dto.getSubject()))
        .body(new InquiryBody(dto.getBody()))
        .statusKbn(dto.getStatusKbn())
        .isReadByUser(dto.getIsReadByUser())
        .createdAt(dto.getCreatedAt())
        .replyModelList(InquiryReplyModelList.from(replyMstList))
        .build();
  }

  /**
   * 新規お問い合わせ登録リクエストとログイン中のアカウント番号からInquiryDetailModelを生成する
   *
   * <p>アカウント番号はリクエストボディではなくセッションから取得した値を用いる（他人になりすましたお問い合わせ登録を防ぐため）
   *
   * @param request {@link InquiryRegistRequest}
   * @param accountNo ログイン中のアカウント番号
   * @return {@link InquiryDetailModel}
   */
  public static InquiryDetailModel from(InquiryRegistRequest request, AccountNo accountNo) {
    return InquiryDetailModel.builder()
        .accountNo(accountNo)
        .subject(new InquirySubject(request.getSubject()))
        .body(new InquiryBody(request.getBody()))
        .build();
  }
}
