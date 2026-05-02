package com.web.gallary.repository;

import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.exception.UpdateFailureException;
import com.web.gallary.model.PhotoDeleteModel;
import com.web.gallary.model.PhotoDetailModel;

/**
 * 写真マスタデータを永続化するRepositoryクラス
 */
public interface PhotoMstRepository {
	/**
	 * 写真マスタを登録する
	 * 
	 * @param	photoDetailModel		{@link PhotoDetailModel}
	 * @param	filePath				写真の保存ファイルパス
	 * @param	newPhotoNo				新規採番した写真番号
	 * @throws	RegistFailureException	登録に失敗した場合
	 */
	void regist(PhotoDetailModel photoDetailModel, String filePath, Integer newPhotoNo) throws RegistFailureException;
	
	/**
	 * 写真マスタを更新する
	 * 
	 * @param	photoDetailModel		{@link PhotoDetailModel}
	 * @throws	UpdateFailureException	更新に失敗した場合
	 */
	void update(PhotoDetailModel photoDetailModel) throws UpdateFailureException;
	
	/**
	 * 写真マスタを削除する
	 * 
	 * @param	photoDeleteModel		{@link PhotoDeleteModel}
	 * @throws	UpdateFailureException	削除に失敗した場合
	 */
	void delete(PhotoDeleteModel photoDeleteModel) throws UpdateFailureException;
	
	/**
	 * アカウント番号から新しい写真番号を発番する
	 * 
	 * @param	accountNo	アカウント番号
	 * @return				新規採番した写真番号
	 */
	Integer getNewPhotoNo(Integer accountNo);
	
	/**
	 * 同じファイル名の写真が存在するかチェックする
	 * 
	 * @param	photoDetailModel	{@link PhotoDetailModel}
	 * @return						写真が存在する場合、true
	 */
	Boolean isExistPhoto(PhotoDetailModel photoDetailModel);
	
	/**
	 * アカウントに登録されている写真の件数を取得する
	 * 
	 * @param	accountNo	アカウント番号
	 * @return				登録件数
	 */
	Integer count(Integer accountNo);
}