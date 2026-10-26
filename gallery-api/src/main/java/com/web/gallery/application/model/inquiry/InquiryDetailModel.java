package com.web.gallery.application.model.inquiry;

import com.web.gallery.domain.enumeration.InquiryStatusEnum;
import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountName;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.inquiry.InquiryBody;
import com.web.gallery.domain.model.inquiry.InquiryId;
import com.web.gallery.domain.model.inquiry.InquiryNo;
import com.web.gallery.domain.model.inquiry.InquirySubject;
import java.time.OffsetDateTime;
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
}
