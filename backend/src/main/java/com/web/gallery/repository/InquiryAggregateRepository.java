package com.web.gallery.repository;

import com.web.gallery.aggregate.Inquiry;
import com.web.gallery.exception.GalleryException;

/** お問い合わせ集約（{@link Inquiry}）を永続化するRepositoryクラス */
public interface InquiryAggregateRepository {
  /**
   * お問い合わせ集約を新規登録する
   *
   * @param inquiry {@link Inquiry}
   * @throws GalleryException 登録に失敗した場合
   */
  void regist(Inquiry inquiry) throws GalleryException;

  /**
   * お問い合わせに返信を追加し、あわせてお問い合わせ本体のステータス・ユーザー既読フラグを更新する
   *
   * @param inquiry {@link Inquiry}
   * @throws GalleryException 更新に失敗した場合
   */
  void addReply(Inquiry inquiry) throws GalleryException;

  /**
   * お問い合わせのユーザー既読フラグを更新する
   *
   * @param inquiry {@link Inquiry}
   * @throws GalleryException 更新に失敗した場合
   */
  void markReadByUser(Inquiry inquiry) throws GalleryException;

  /**
   * お問い合わせを取り下げる
   *
   * @param inquiry {@link Inquiry}
   * @throws GalleryException 更新に失敗した場合
   */
  void withdraw(Inquiry inquiry) throws GalleryException;
}
