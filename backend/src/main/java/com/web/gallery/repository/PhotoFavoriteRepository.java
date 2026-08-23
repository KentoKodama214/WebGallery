package com.web.gallery.repository;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.model.PhotoFavoriteDeleteModel;
import com.web.gallery.model.PhotoFavoriteModel;

/**
 * 写真お気に入りデータを永続化するRepositoryクラス
 */
public interface PhotoFavoriteRepository {
	/**
	 * 写真お気に入りを登録する
	 * 
	 * @param	favoriteModel		{@link PhotoFavoriteModel}
	 * @throws	GalleryException	登録に失敗した場合
	 */
	void regist(PhotoFavoriteModel favoriteModel) throws GalleryException;

	/**
	 * 写真お気に入りを削除する
	 *
	 * @param	favoriteDeleteModel	{@link PhotoFavoriteDeleteModel}
	 * @throws	GalleryException	更新に失敗した場合
	 */
	void delete(PhotoFavoriteDeleteModel favoriteDeleteModel) throws GalleryException;
	
	/**
	 * 該当写真の写真お気に入りを全件削除する
	 *
	 * @param	favoriteDeleteModel	{@link PhotoFavoriteDeleteModel}
	 */
	void clear(PhotoFavoriteDeleteModel favoriteDeleteModel);

	/**
	 * アカウント番号で自分が登録した写真お気に入りを全件削除する
	 *
	 * @param	accountNo	アカウント番号
	 */
	void deleteByAccountNo(AccountNo accountNo);

	/**
	 * アカウント番号で自分の写真に対する他人の写真お気に入りを全件削除する
	 *
	 * @param	favoritePhotoAccountNo	お気に入り写真アカウント番号
	 */
	void deleteByFavoritePhotoAccountNo(AccountNo favoritePhotoAccountNo);
}