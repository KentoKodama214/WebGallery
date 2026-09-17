package com.web.gallery.aggregate;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.inquiry.InquiryId;
import com.web.gallery.domain.inquiry.InquiryNo;
import com.web.gallery.domain.inquiry.ReplyBody;
import com.web.gallery.domain.inquiry.ReplyNo;
import com.web.gallery.enumeration.InquiryStatusEnum;
import com.web.gallery.model.InquiryDetailModel;
import com.web.gallery.model.InquiryReplyModel;
import com.web.gallery.model.InquiryReplyModelList;

/** お問い合わせ・返信のライフサイクルを管理する集約ルートクラス */
public class Inquiry {

  /** アカウント番号（お問い合わせ所有者） */
  private final AccountNo accountNo;

  /** お問い合わせ番号 */
  private final InquiryNo inquiryNo;

  /** ID（登録済みのお問い合わせを復元した場合のみ設定） */
  private final InquiryId inquiryId;

  /** お問い合わせの詳細情報（返信を含む） */
  private InquiryDetailModel detail;

  /** 今回追加する返信（{@link #addReply}実行後のみ設定） */
  private InquiryReplyModel newReply;

  private Inquiry(
      AccountNo accountNo, InquiryNo inquiryNo, InquiryId inquiryId, InquiryDetailModel detail) {
    this.accountNo = accountNo;
    this.inquiryNo = inquiryNo;
    this.inquiryId = inquiryId;
    this.detail = detail;
  }

  /**
   * 新規登録用のInquiryを生成する
   *
   * @param requestDetail 登録リクエストのお問い合わせ詳細情報
   * @param newInquiryNo 新規採番されたお問い合わせ番号
   * @return {@link Inquiry}
   */
  public static Inquiry forRegist(InquiryDetailModel requestDetail, InquiryNo newInquiryNo) {
    InquiryDetailModel detail =
        requestDetail.toBuilder()
            .inquiryNo(newInquiryNo)
            .statusKbn(InquiryStatusEnum.UNREPLIED)
            .isReadByUser(true)
            .build();
    return new Inquiry(requestDetail.getAccountNo(), newInquiryNo, null, detail);
  }

  /**
   * 登録済みのお問い合わせから、返信追加・既読化のためのInquiryを復元する
   *
   * @param existingDetail 登録済みのお問い合わせ詳細情報
   * @param existingReplies 登録済みの返信一覧
   * @return {@link Inquiry}
   */
  public static Inquiry reconstruct(
      InquiryDetailModel existingDetail, InquiryReplyModelList existingReplies) {
    InquiryDetailModel detail = existingDetail.toBuilder().replyModelList(existingReplies).build();
    return new Inquiry(
        existingDetail.getAccountNo(),
        existingDetail.getInquiryNo(),
        existingDetail.getInquiryId(),
        detail);
  }

  /**
   * 返信を追加する
   *
   * <p>ステータスを回答済みへ、ユーザー既読フラグを未読へ自動的に遷移させる
   *
   * @param adminAccountNo 返信した管理者のアカウント番号
   * @param replyBody 返信本文
   * @param newReplyNo 新規採番された返信番号
   */
  public void addReply(AccountNo adminAccountNo, ReplyBody replyBody, ReplyNo newReplyNo) {
    this.newReply =
        InquiryReplyModel.builder()
            .inquiryId(this.inquiryId)
            .replyNo(newReplyNo)
            .adminAccountNo(adminAccountNo)
            .body(replyBody)
            .build();
    this.detail =
        this.detail.toBuilder().statusKbn(InquiryStatusEnum.REPLIED).isReadByUser(false).build();
  }

  /** ユーザーが返信を閲覧したことをマークする */
  public void markReadByUser() {
    this.detail = this.detail.toBuilder().isReadByUser(true).build();
  }

  /**
   * アカウント番号を取得する
   *
   * @return アカウント番号
   */
  public AccountNo getAccountNo() {
    return accountNo;
  }

  /**
   * お問い合わせ番号を取得する
   *
   * @return お問い合わせ番号
   */
  public InquiryNo getInquiryNo() {
    return inquiryNo;
  }

  /**
   * IDを取得する
   *
   * <p>{@link #forRegist}で生成したInquiryでは呼び出せない（登録前のためIDが未確定）
   *
   * @return ID
   */
  public InquiryId getInquiryId() {
    return inquiryId;
  }

  /**
   * お問い合わせの詳細情報を取得する
   *
   * @return {@link InquiryDetailModel}
   */
  public InquiryDetailModel getDetail() {
    return detail;
  }

  /**
   * 今回追加する返信を取得する
   *
   * <p>{@link #addReply}実行前は呼び出せない（返信が未生成のため）
   *
   * @return {@link InquiryReplyModel}
   */
  public InquiryReplyModel getNewReply() {
    return newReply;
  }
}
