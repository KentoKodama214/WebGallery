package com.web.gallery.repository;

import java.util.List;

import com.web.gallery.exception.PhotoNotFoundException;
import com.web.gallery.model.PhotoDetailGetModel;
import com.web.gallery.model.PhotoDetailModel;
import com.web.gallery.model.PhotoGetModel;
import com.web.gallery.model.PhotoModel;

/**
 * 写真のメタデータを含めた詳細情報を永続化するRepositoryクラス
 */
public interface PhotoDetailRepository {
	/**
	 * 該当アカウントの写真の一覧を取得する
	 * 
	 * @param	photoGetModel	{@link PhotoGetModel}
	 * @return						{@link PhotoModel}
	 */
	List<PhotoModel> getPhotoList(PhotoGetModel photoGetModel);
	
	/**
	 * 写真のメタデータを含めた詳細情報を取得する
	 * 
	 * @param	photoDetailGetModel		{@link PhotoDetailGetModel}
	 * @return							{@link PhotoDetailModel}
	 * @throws	PhotoNotFoundException	写真が存在しなかった場合
	 */
	PhotoDetailModel getPhotoDetail(PhotoDetailGetModel photoDetailGetModel) throws PhotoNotFoundException;
}