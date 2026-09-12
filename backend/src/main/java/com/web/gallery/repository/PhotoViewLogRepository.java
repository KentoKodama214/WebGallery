package com.web.gallery.repository;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.model.PhotoViewLogModel;

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
