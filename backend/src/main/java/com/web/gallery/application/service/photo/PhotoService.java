package com.web.gallery.application.service.photo;

import com.web.gallery.application.model.photo.PhotoDeleteModelList;
import com.web.gallery.application.model.photo.PhotoDetailGetModel;
import com.web.gallery.application.model.photo.PhotoDetailModel;
import com.web.gallery.application.model.photo.PhotoDetailModelList;
import com.web.gallery.application.model.photo.PhotoListGetModel;
import com.web.gallery.application.model.photo.PhotoPageModel;
import com.web.gallery.application.model.photo.PhotoSaveResultModel;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.model.account.AccountNo;

/** 写真に関するビジネスロジックを行うServiceクラス */
public interface PhotoService {
  /**
   * 写真一覧を、ページング情報に従い取得する
   *
   * @param photoListGetModel {@link PhotoListGetModel}
   * @return {@link PhotoPageModel}
   * @throws GalleryException 指定のアカウントが存在しなかった場合
   */
  PhotoPageModel getPhotoList(PhotoListGetModel photoListGetModel) throws GalleryException;

  /**
   * 写真のメタデータを含めた詳細情報を取得する
   *
   * @param photoDetailGetModel {@link PhotoDetailGetModel}
   * @return {@link PhotoDetailModel}
   * @throws GalleryException 写真、または指定のアカウントが存在しなかった場合
   */
  PhotoDetailModel getPhotoDetail(PhotoDetailGetModel photoDetailGetModel) throws GalleryException;

  /**
   * 写真を登録・更新する
   *
   * @param photoDetailModelList {@link PhotoDetailModelList}
   * @throws GalleryException 以下のいずれかに該当する場合 ・新規登録時に画像ファイルが指定されていない場合 ・許可されていない拡張子のファイルの場合
   *     ・画像ファイルのContent-Typeが許可されていない場合 ・画像ファイルのマジックバイトが既知の画像フォーマットと一致しない場合 ・画像ファイルのサイズが上限を超えている場合
   *     ・同じファイル名のファイルが既に保存済みの場合 ・登録に失敗した場合 ・更新に失敗した場合
   * @return {@link PhotoSaveResultModel}
   */
  PhotoSaveResultModel savePhotos(PhotoDetailModelList photoDetailModelList)
      throws GalleryException;

  /**
   * 写真を削除する
   *
   * @param photoDeleteModelList {@link PhotoDeleteModelList}
   * @throws GalleryException 削除に失敗した場合
   */
  void deletePhotos(PhotoDeleteModelList photoDeleteModelList) throws GalleryException;

  /**
   * 該当アカウントが写真の登録枚数の上限に達しているかチェックする
   *
   * @param accountNo アカウント番号
   * @return 上限に達している場合、true
   */
  Boolean isReachedUpperLimit(AccountNo accountNo);

  /**
   * 該当アカウントの残り写真登録可能枚数を取得する
   *
   * @param accountNo アカウント番号
   * @return 残り登録可能枚数。上限が存在しない権限区分の場合はnull
   */
  Integer getRemainingPhotoCount(AccountNo accountNo);
}
