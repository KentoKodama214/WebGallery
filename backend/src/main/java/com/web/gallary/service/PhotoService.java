package com.web.gallary.service;

import java.util.List;

import com.web.gallary.exception.FileDuplicateException;
import com.web.gallary.exception.PhotoNotFoundException;
import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.exception.UpdateFailureException;
import com.web.gallary.model.PhotoDeleteModel;
import com.web.gallary.model.PhotoDetailGetModel;
import com.web.gallary.model.PhotoDetailModel;
import com.web.gallary.model.PhotoListGetModel;
import com.web.gallary.model.PhotoModel;

/**
 * 写真に関するビジネスロジックを行うServiceクラス
 */
public interface PhotoService {
	/**
	 * 写真一覧を取得する
	 * 
	 * @param	photoListGetModel	{@link PhotoListGetModel}
	 * @return						{@link PhotoModel}
	 */
	List<PhotoModel> getPhotoList(PhotoListGetModel photoListGetModel);
	
	/**
	 * 写真のメタデータを含めた詳細情報を取得する
	 * 
	 * @param	photoDetailGetModel		{@link PhotoDetailGetModel}
	 * @return							{@link PhotoDetailModel}
	 * @throws	PhotoNotFoundException	写真が存在しなかった場合
	 */
	PhotoDetailModel getPhotoDetail(PhotoDetailGetModel photoDetailGetModel) throws PhotoNotFoundException;
	
	/**
	 * 写真を登録・更新する
	 * 
	 * @param	accountId				アカウントID
	 * @param	photoDetailModelList	{@link PhotoDetailModel}
	 * @throws	FileDuplicateException 	同じファイル名のファイルが既に保存済みの場合
	 * @throws	RegistFailureException	登録に失敗した場合
	 * @throws	UpdateFailureException	更新に失敗した場合
	 */
	void savePhotos(String accountId, List<PhotoDetailModel> photoDetailModelList) throws FileDuplicateException, RegistFailureException, UpdateFailureException;
	
	/**
	 * 写真を削除する
	 * 
	 * @param	accountId				アカウントID
	 * @param	photoDeleteModelList	{@link PhotoDeleteModel}
	 * @throws	UpdateFailureException	削除に失敗した場合
	 */
	void deletePhotos(String accountId, List<PhotoDeleteModel> photoDeleteModelList) throws UpdateFailureException;
	
	/**
	 * 該当アカウントが写真の登録枚数の上限に達しているかチェックする
	 * 
	 * @param	accountNo	アカウント番号
	 * @return				上限に達している場合、true
	 */
	Boolean isReachedUpperLimit(Integer accountNo);
}