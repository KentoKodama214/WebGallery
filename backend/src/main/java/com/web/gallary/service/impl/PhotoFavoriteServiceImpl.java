package com.web.gallary.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.exception.UpdateFailureException;
import com.web.gallary.model.PhotoFavoriteDeleteModel;
import com.web.gallary.model.PhotoFavoriteModel;
import com.web.gallary.repository.PhotoFavoriteRepository;
import com.web.gallary.service.PhotoFavoriteService;

import lombok.RequiredArgsConstructor;

/**
 * 写真お気に入りに関するビジネスロジックを行うServiceの実装クラス
 */
@Service
@RequiredArgsConstructor
public class PhotoFavoriteServiceImpl implements PhotoFavoriteService {
	
	private final PhotoFavoriteRepository photoFavoriteRepository;
	
	/**
	 * お気に入りを追加する
	 * 
	 * @param	photoFavoriteModel		{@link PhotoFavoriteModel}
	 * @throws	RegistFailureException	登録に失敗した場合
	 */
	@Override
	@Transactional
	public void addFavorite(PhotoFavoriteModel photoFavoriteModel) throws RegistFailureException {
		photoFavoriteRepository.regist(photoFavoriteModel);
	}
	
	/**
	 * お気に入りを解除する
	 * 
	 * @param	photoFavoriteModel		{@link PhotoFavoriteModel}
	 * @throws	UpdateFailureException	解除に失敗した場合
	 */
	@Override
	@Transactional
	public void deleteFavorite(PhotoFavoriteModel photoFavoriteModel) throws UpdateFailureException {
		photoFavoriteRepository.delete(PhotoFavoriteDeleteModel.builder()
				.accountNo(photoFavoriteModel.getAccountNo())
				.favoritePhotoAccountNo(photoFavoriteModel.getFavoritePhotoAccountNo())
				.favoritePhotoNo(photoFavoriteModel.getFavoritePhotoNo())
				.build());
	}
}