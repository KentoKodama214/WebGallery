package com.web.gallery.application.repository;

import com.web.gallery.application.model.photo.PhotoViewLogModel;
import com.web.gallery.domain.model.account.AccountNo;

/**
 * 写真詳細閲覧ログデータを永続化するRepositoryクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
public interface PhotoViewLogRepository {
  /**
   * 写真詳細閲覧ログを保存する
   *
   * @param photoViewLogModel {@link PhotoViewLogModel}
   */
  void save(PhotoViewLogModel photoViewLogModel);

  /**
   * 写真アカウント番号に該当する写真詳細閲覧ログを削除する
   *
   * @param photoAccountNo 写真アカウント番号
   */
  void deleteByPhotoAccountNo(AccountNo photoAccountNo);
}
