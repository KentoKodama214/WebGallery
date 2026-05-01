package com.web.gallary.repository;

import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.exception.UpdateFailureException;
import com.web.gallary.model.PhotoFavoriteDeleteModel;
import com.web.gallary.model.PhotoFavoriteModel;

/**
 * 写真お気に入りデータを永続化するRepositoryクラス
 */
public interface PhotoFavoriteRepository {
	/**
	 * 写真お気に入りを登録する
	 * 
	 * @param	favoriteModel			{@link PhotoFavoriteModel}
	 * @throws	RegistFailureException	登録に失敗した場合
	 */
	void regist(PhotoFavoriteModel favoriteModel) throws RegistFailureException;
	
	/**
	 * 写真お気に入りを削除する
	 * 
	 * @param	favoriteDeleteModel		{@link PhotoFavoriteDeleteModel}
	 * @throws	UpdateFailureException	更新に失敗した場合
	 */
	void delete(PhotoFavoriteDeleteModel favoriteDeleteModel) throws UpdateFailureException;
	
	/**
	 * 該当写真の写真お気に入りを全件削除する
	 * 
	 * @param	favoriteDeleteModel	{@link PhotoFavoriteDeleteModel}
	 */
	void clear(PhotoFavoriteDeleteModel favoriteDeleteModel);
}