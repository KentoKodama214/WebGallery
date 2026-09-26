package com.web.gallery.application.service;

import com.web.gallery.application.model.photo.PhotoFavoriteModel;
import com.web.gallery.domain.exception.GalleryException;

/** 写真お気に入りに関するビジネスロジックを行うServiceクラス */
public interface PhotoFavoriteService {
  /**
   * お気に入りを追加する
   *
   * @param photoFavoriteModel {@link PhotoFavoriteModel}
   * @throws GalleryException 登録に失敗した場合
   */
  void addFavorite(PhotoFavoriteModel photoFavoriteModel) throws GalleryException;

  /**
   * お気に入りを解除する
   *
   * @param photoFavoriteModel {@link PhotoFavoriteModel}
   * @throws GalleryException 解除に失敗した場合
   */
  void deleteFavorite(PhotoFavoriteModel photoFavoriteModel) throws GalleryException;
}
