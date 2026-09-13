package com.web.gallery.repository;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.model.PhotoListFilterLogModel;

/**
 * 写真一覧絞り込みログデータを永続化するRepositoryクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
public interface PhotoListFilterLogRepository {
  /**
   * 写真一覧絞り込みログを保存する
   *
   * @param photoListFilterLogModel {@link PhotoListFilterLogModel}
   */
  void save(PhotoListFilterLogModel photoListFilterLogModel);

  /**
   * 写真アカウント番号に該当する写真一覧絞り込みログを削除する
   *
   * @param photoAccountNo 写真アカウント番号
   */
  void deleteByPhotoAccountNo(AccountNo photoAccountNo);
}
