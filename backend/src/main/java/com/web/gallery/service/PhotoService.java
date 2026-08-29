package com.web.gallery.service;

import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.model.PhotoDeleteModelList;
import com.web.gallery.model.PhotoDetailGetModel;
import com.web.gallery.model.PhotoDetailModel;
import com.web.gallery.model.PhotoDetailModelList;
import com.web.gallery.model.PhotoListGetModel;
import com.web.gallery.model.PhotoModelList;

/**
 * 写真に関するビジネスロジックを行うServiceクラス
 */
public interface PhotoService {
	/**
	 * 写真一覧を取得する
	 *
	 * @param	photoListGetModel	{@link PhotoListGetModel}
	 * @return						{@link PhotoModelList}
	 * @throws	GalleryException	指定のアカウントが存在しなかった場合
	 */
	PhotoModelList getPhotoList(PhotoListGetModel photoListGetModel) throws GalleryException;
	
	/**
	 * 写真のメタデータを含めた詳細情報を取得する
	 *
	 * @param	photoDetailGetModel	{@link PhotoDetailGetModel}
	 * @return						{@link PhotoDetailModel}
	 * @throws	GalleryException	写真、または指定のアカウントが存在しなかった場合
	 */
	PhotoDetailModel getPhotoDetail(PhotoDetailGetModel photoDetailGetModel) throws GalleryException;

	/**
	 * 写真を登録・更新する
	 *
	 * @param	accountId				アカウントID
	 * @param	photoDetailModelList	{@link PhotoDetailModelList}
	 * @throws	GalleryException		以下のいずれかに該当する場合
	 *                              	・許可されていない拡張子のファイルの場合
	 *                              	・同じファイル名のファイルが既に保存済みの場合
	 *                              	・登録に失敗した場合
	 *                              	・更新に失敗した場合
	 * @return							登録・更新した写真番号
	 */
	PhotoNo savePhotos(AccountId accountId, PhotoDetailModelList photoDetailModelList) throws GalleryException;

	/**
	 * 写真を削除する
	 *
	 * @param	accountId				アカウントID
	 * @param	photoDeleteModelList	{@link PhotoDeleteModelList}
	 * @throws	GalleryException		削除に失敗した場合
	 */
	void deletePhotos(AccountId accountId, PhotoDeleteModelList photoDeleteModelList) throws GalleryException;
	
	/**
	 * 該当アカウントが写真の登録枚数の上限に達しているかチェックする
	 * 
	 * @param	accountNo	アカウント番号
	 * @return				上限に達している場合、true
	 */
	Boolean isReachedUpperLimit(AccountNo accountNo);
}