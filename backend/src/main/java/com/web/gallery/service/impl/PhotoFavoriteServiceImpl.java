package com.web.gallery.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web.gallery.exception.GalleryException;
import com.web.gallery.model.PhotoFavoriteDeleteModel;
import com.web.gallery.model.PhotoFavoriteModel;
import com.web.gallery.repository.PhotoFavoriteRepository;
import com.web.gallery.service.PhotoFavoriteService;

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
	 * @param	photoFavoriteModel	{@link PhotoFavoriteModel}
	 * @throws	GalleryException	登録に失敗した場合
	 */
	@Override
	@Transactional(rollbackFor = GalleryException.class)
	public void addFavorite(PhotoFavoriteModel photoFavoriteModel) throws GalleryException {
		photoFavoriteRepository.regist(photoFavoriteModel);
	}

	/**
	 * お気に入りを解除する
	 *
	 * @param	photoFavoriteModel	{@link PhotoFavoriteModel}
	 * @throws	GalleryException	解除に失敗した場合
	 */
	@Override
	@Transactional(rollbackFor = GalleryException.class)
	public void deleteFavorite(PhotoFavoriteModel photoFavoriteModel) throws GalleryException {
		photoFavoriteRepository.delete(PhotoFavoriteDeleteModel.from(photoFavoriteModel));
	}
}