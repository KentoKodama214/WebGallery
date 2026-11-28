package com.web.gallery.application.repository.photo;

import com.web.gallery.application.model.photo.AdminPhotoGetModel;
import com.web.gallery.application.model.photo.AdminPhotoPageModel;
import com.web.gallery.application.model.photo.PhotoDetailModel;
import com.web.gallery.application.model.photo.PhotoDetailSearchModel;
import com.web.gallery.application.model.photo.PhotoGetModel;
import com.web.gallery.application.model.photo.PhotoPageModel;
import com.web.gallery.domain.exception.GalleryException;

/** 写真のメタデータを含めた詳細情報を永続化するRepositoryクラス */
public interface PhotoDetailRepository {
  /**
   * 該当アカウントの写真の一覧を、ページング情報に従い取得する
   *
   * @param photoGetModel {@link PhotoGetModel}
   * @return {@link PhotoPageModel}
   */
  PhotoPageModel getPhotoList(PhotoGetModel photoGetModel);

  /**
   * 写真のメタデータを含めた詳細情報を取得する
   *
   * @param photoDetailSearchModel {@link PhotoDetailSearchModel}
   * @return {@link PhotoDetailModel}
   * @throws GalleryException 写真が存在しなかった場合
   */
  PhotoDetailModel getPhotoDetail(PhotoDetailSearchModel photoDetailSearchModel)
      throws GalleryException;

  /**
   * 全アカウントの写真の一覧を、ページング情報に従い取得する（管理者用）
   *
   * <p>管理者により非公開にされた写真も含め、登録日時の新しい順に取得する
   *
   * @param adminPhotoGetModel {@link AdminPhotoGetModel}
   * @return {@link AdminPhotoPageModel}
   */
  AdminPhotoPageModel getPhotoListForAdmin(AdminPhotoGetModel adminPhotoGetModel);
}
