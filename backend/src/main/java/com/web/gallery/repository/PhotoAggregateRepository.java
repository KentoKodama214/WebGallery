package com.web.gallery.repository;

import com.web.gallery.aggregate.Photo;
import com.web.gallery.exception.GalleryException;

/** 写真集約（{@link Photo}）を永続化するRepositoryクラス */
public interface PhotoAggregateRepository {
  /**
   * 写真集約を新規登録する
   *
   * @param photo {@link Photo}
   * @throws GalleryException 以下のいずれかに該当する場合 ・同じファイル名の写真が既に保存済みの場合 ・登録に失敗した場合
   */
  void regist(Photo photo) throws GalleryException;

  /**
   * 写真集約を更新する
   *
   * @param photo {@link Photo}
   * @throws GalleryException 更新に失敗した場合
   */
  void update(Photo photo) throws GalleryException;

  /**
   * 写真集約を削除する
   *
   * @param photo {@link Photo}
   * @throws GalleryException 削除に失敗した場合
   */
  void delete(Photo photo) throws GalleryException;
}
