package com.web.gallery.entity;

import com.web.gallery.model.PhotoFavoriteDeleteModel;
import lombok.Builder;
import lombok.Data;

/** 写真お気に入りテーブルの抽出条件クラス */
@Data
@Builder
public class PhotoFavoriteCondition {
  /** アカウント番号 */
  private Long accountNo;

  /** お気に入り写真アカウント番号 */
  private Long favoritePhotoAccountNo;

  /** お気に入り写真番号 */
  private Long favoritePhotoNo;

  /**
   * PhotoFavoriteDeleteModelから抽出条件を生成する
   *
   * @param model {@link PhotoFavoriteDeleteModel}
   * @return {@link PhotoFavoriteCondition}
   */
  public static PhotoFavoriteCondition from(PhotoFavoriteDeleteModel model) {
    return PhotoFavoriteCondition.builder()
        .accountNo(model.getAccountNo().value())
        .favoritePhotoAccountNo(model.getFavoritePhotoAccountNo().value())
        .favoritePhotoNo(model.getFavoritePhotoNo().value())
        .build();
  }

  /**
   * 写真お気に入り全件削除用のPhotoFavoriteDeleteModelから抽出条件を生成する
   *
   * @param model {@link PhotoFavoriteDeleteModel}
   * @return {@link PhotoFavoriteCondition}
   */
  public static PhotoFavoriteCondition forClear(PhotoFavoriteDeleteModel model) {
    return PhotoFavoriteCondition.builder()
        .favoritePhotoAccountNo(model.getFavoritePhotoAccountNo().value())
        .favoritePhotoNo(model.getFavoritePhotoNo().value())
        .build();
  }

  /**
   * アカウント番号で自分が登録したお気に入り削除用の抽出条件を生成する
   *
   * @param accountNo アカウント番号
   * @return {@link PhotoFavoriteCondition}
   */
  public static PhotoFavoriteCondition byAccountNo(Long accountNo) {
    return PhotoFavoriteCondition.builder().accountNo(accountNo).build();
  }

  /**
   * アカウント番号で自分の写真に対する他人のお気に入り削除用の抽出条件を生成する
   *
   * @param favoritePhotoAccountNo お気に入り写真アカウント番号
   * @return {@link PhotoFavoriteCondition}
   */
  public static PhotoFavoriteCondition byFavoritePhotoAccountNo(Long favoritePhotoAccountNo) {
    return PhotoFavoriteCondition.builder().favoritePhotoAccountNo(favoritePhotoAccountNo).build();
  }
}
