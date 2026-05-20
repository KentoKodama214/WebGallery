package com.web.gallery.service;

import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.model.PhotoFavoriteModel;

/**
 * 写真お気に入りに関するビジネスロジックを行うServiceクラス
 */
public interface PhotoFavoriteService {
	/**
	 * お気に入りを追加する
	 * 
	 * @param	photoFavoriteModel		{@link PhotoFavoriteModel}
	 * @throws	RegistFailureException	登録に失敗した場合
	 */
	void addFavorite(PhotoFavoriteModel photoFavoriteModel) throws RegistFailureException;
	
	/**
	 * お気に入りを解除する
	 * 
	 * @param	photoFavoriteModel		{@link PhotoFavoriteModel}
	 * @throws	UpdateFailureException	解除に失敗した場合
	 */
	void deleteFavorite(PhotoFavoriteModel photoFavoriteModel) throws UpdateFailureException;
}