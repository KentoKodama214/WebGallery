package com.web.gallery.model;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * 写真お気に入りを解除する時の情報を受け渡すためのModelクラス
 */
@Value
@Builder
public class PhotoFavoriteDeleteModel {
	/** アカウント番号 */
	private AccountNo accountNo;

	/** お気に入り写真アカウント番号 */
	@NonNull
	private AccountNo favoritePhotoAccountNo;

	/** 写真番号 */
	@NonNull
	private PhotoNo favoritePhotoNo;

	/**
	 * PhotoFavoriteModelからPhotoFavoriteDeleteModelを生成する
	 *
	 * @param	model	{@link PhotoFavoriteModel}
	 * @return			{@link PhotoFavoriteDeleteModel}
	 */
	public static PhotoFavoriteDeleteModel from(PhotoFavoriteModel model) {
		return PhotoFavoriteDeleteModel.builder()
				.accountNo(model.getAccountNo())
				.favoritePhotoAccountNo(model.getFavoritePhotoAccountNo())
				.favoritePhotoNo(model.getFavoritePhotoNo())
				.build();
	}

	/**
	 * PhotoDeleteModelからPhotoFavoriteDeleteModelを生成する
	 *
	 * @param	model	{@link PhotoDeleteModel}
	 * @return			{@link PhotoFavoriteDeleteModel}
	 */
	public static PhotoFavoriteDeleteModel from(PhotoDeleteModel model) {
		return PhotoFavoriteDeleteModel.builder()
				.favoritePhotoAccountNo(model.getAccountNo())
				.favoritePhotoNo(model.getPhotoNo())
				.build();
	}
}
