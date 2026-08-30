package com.web.gallery.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.model.PhotoDetailSearchModel;
import com.web.gallery.model.PhotoFavoriteDeleteModel;
import com.web.gallery.model.PhotoFavoriteModel;
import com.web.gallery.repository.PhotoDetailRepository;
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
	private final PhotoDetailRepository photoDetailRepository;

	/**
	 * お気に入りを追加する<p>
	 * 自分自身の写真に対するお気に入り登録は、お気に入り数の水増しを防ぐため拒否する。
	 * また、実在しない写真に対する登録も拒否する
	 *
	 * @param	photoFavoriteModel	{@link PhotoFavoriteModel}
	 * @throws	GalleryException	以下のいずれかに該当する場合
	 *                              	・自分自身の写真をお気に入り登録しようとした場合
	 *                              	・対象の写真が存在しない場合
	 *                              	・登録に失敗した場合
	 */
	@Override
	@Transactional(rollbackFor = GalleryException.class)
	public void addFavorite(PhotoFavoriteModel photoFavoriteModel) throws GalleryException {
		if (photoFavoriteModel.getFavoritePhotoAccountNo().equals(photoFavoriteModel.getAccountNo())) {
			throw ErrorEnum.INVALID_INPUT.toException();
		}

		// 対象写真の存在を検証する（存在しない場合はPHOTO_NOT_FOUNDが送出される）
		photoDetailRepository.getPhotoDetail(PhotoDetailSearchModel.builder()
				.photoAccountNo(photoFavoriteModel.getFavoritePhotoAccountNo())
				.photoNo(photoFavoriteModel.getFavoritePhotoNo())
				.build());

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
