package com.web.gallery.repository;

import com.web.gallery.exception.GalleryException;
import com.web.gallery.model.PhotoDetailModel;
import com.web.gallery.model.PhotoDetailSearchModel;
import com.web.gallery.model.PhotoGetModel;
import com.web.gallery.model.PhotoModelList;

/**
 * 写真のメタデータを含めた詳細情報を永続化するRepositoryクラス
 */
public interface PhotoDetailRepository {
	/**
	 * 該当アカウントの写真の一覧を取得する
	 *
	 * @param	photoGetModel	{@link PhotoGetModel}
	 * @return						{@link PhotoModelList}
	 */
	PhotoModelList getPhotoList(PhotoGetModel photoGetModel);
	
	/**
	 * 写真のメタデータを含めた詳細情報を取得する
	 * 
	 * @param	photoDetailSearchModel	{@link PhotoDetailSearchModel}
	 * @return							{@link PhotoDetailModel}
	 * @throws	GalleryException		写真が存在しなかった場合
	 */
	PhotoDetailModel getPhotoDetail(PhotoDetailSearchModel photoDetailSearchModel) throws GalleryException;
}