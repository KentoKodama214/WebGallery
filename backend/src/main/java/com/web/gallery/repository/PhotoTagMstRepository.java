package com.web.gallery.repository;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.model.PhotoTagDeleteModel;
import com.web.gallery.model.PhotoTagModel;

/** 写真タグマスタデータを永続化するRepositoryクラス */
public interface PhotoTagMstRepository {
  /**
   * 写真タグマスタを登録する
   *
   * @param photoTagModel {@link PhotoTagModel}
   * @throws GalleryException 登録に失敗した場合
   */
  void regist(PhotoTagModel photoTagModel) throws GalleryException;

  /**
   * 該当写真の写真タグを全件削除する
   *
   * @param photoTagDeleteModel {@link PhotoTagDeleteModel}
   */
  void clear(PhotoTagDeleteModel photoTagDeleteModel);

  /**
   * アカウント番号で写真タグを全件削除する
   *
   * @param accountNo アカウント番号
   */
  void deleteByAccountNo(AccountNo accountNo);
}
