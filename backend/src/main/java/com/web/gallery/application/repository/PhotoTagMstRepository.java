package com.web.gallery.application.repository;

import com.web.gallery.application.model.photo.PhotoTagDeleteModel;
import com.web.gallery.application.model.photo.PhotoTagModel;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.model.account.AccountNo;

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
