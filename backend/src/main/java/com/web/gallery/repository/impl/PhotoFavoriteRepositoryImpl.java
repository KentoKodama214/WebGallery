package com.web.gallery.repository.impl;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.entity.PhotoFavorite;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.mapper.PhotoFavoriteMapper;
import com.web.gallery.model.PhotoFavoriteDeleteModel;
import com.web.gallery.model.PhotoFavoriteModel;
import com.web.gallery.repository.PhotoFavoriteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 写真お気に入りデータを永続化するRepositoryの実装クラス
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PhotoFavoriteRepositoryImpl implements PhotoFavoriteRepository{

	private final PhotoFavoriteMapper photoFavoriteMapper;

	/**
	 * 写真お気に入りを登録する
	 *
	 * @param	favoriteModel			{@link PhotoFavoriteModel}
	 * @throws	RegistFailureException	登録に失敗した場合
	 */
	@Override
	public void regist(PhotoFavoriteModel favoriteModel) throws RegistFailureException {
		PhotoFavorite photoFavorite = PhotoFavorite.from(favoriteModel);

		try {
			photoFavoriteMapper.insert(photoFavorite);
		}
		catch (DuplicateKeyException e) {
			log.warn("PhotoFavorite: Duplicate Key (AccountNo: {}, FavoritePhotoAccountNo: {}, FavoritePhotoNo: {})",
					favoriteModel.getAccountNo().value(), favoriteModel.getFavoritePhotoAccountNo().value(), favoriteModel.getFavoritePhotoNo().value(), e);
			throw new RegistFailureException(ErrorEnum.FAIL_TO_REGIST_FAVORITE);
		}
	}

	/**
	 * 写真お気に入りを削除する
	 *
	 * @param	favoriteDeleteModel		{@link PhotoFavoriteDeleteModel}
	 * @throws	UpdateFailureException	更新に失敗した場合
	 */
	@Override
	public void delete(PhotoFavoriteDeleteModel favoriteDeleteModel) throws UpdateFailureException {
		PhotoFavorite photoFavorite = PhotoFavorite.from(favoriteDeleteModel);

		if (photoFavoriteMapper.delete(photoFavorite) < 1) {
			log.warn("PhotoFavorite: Delete Failed (AccountNo: {}, FavoritePhotoAccountNo: {}, FavoritePhotoNo: {})",
					favoriteDeleteModel.getAccountNo() != null ? favoriteDeleteModel.getAccountNo().value() : null,
					favoriteDeleteModel.getFavoritePhotoAccountNo().value(), favoriteDeleteModel.getFavoritePhotoNo().value());
			throw new UpdateFailureException(ErrorEnum.FAIL_TO_CANCEL_FAVORITE);
		}
	}

	/**
	 * 該当写真の写真お気に入りを全件削除する
	 *
	 * @param	favoriteDeleteModel	{@link PhotoFavoriteDeleteModel}
	 */
	@Override
	public void clear(PhotoFavoriteDeleteModel favoriteDeleteModel) {
		PhotoFavorite photoFavorite = PhotoFavorite.fromForClear(favoriteDeleteModel);
		photoFavoriteMapper.delete(photoFavorite);
	}

	/**
	 * アカウント番号で自分が登録した写真お気に入りを全件削除する
	 *
	 * @param	accountNo	アカウント番号
	 */
	@Override
	public void deleteByAccountNo(AccountNo accountNo) {
		photoFavoriteMapper.delete(PhotoFavorite.conditionByAccountNo(accountNo));
	}

	/**
	 * アカウント番号で自分の写真に対する他人の写真お気に入りを全件削除する
	 *
	 * @param	favoritePhotoAccountNo	お気に入り写真アカウント番号
	 */
	@Override
	public void deleteByFavoritePhotoAccountNo(AccountNo favoritePhotoAccountNo) {
		photoFavoriteMapper.delete(PhotoFavorite.conditionByFavoritePhotoAccountNo(favoritePhotoAccountNo));
	}
}
