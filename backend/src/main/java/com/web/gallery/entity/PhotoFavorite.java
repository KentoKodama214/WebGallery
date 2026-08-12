package com.web.gallery.entity;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.CreatedBy;
import com.web.gallery.domain.common.CreatedAt;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.model.PhotoFavoriteDeleteModel;
import com.web.gallery.model.PhotoFavoriteModel;

import lombok.Builder;
import lombok.Data;

/**
 * 写真お気に入りテーブルのEntityクラス
 */
@Data
@Builder
public class PhotoFavorite {
	/** ID */
	private Long id;

	/** アカウント番号 */
	private AccountNo accountNo;

	/** お気に入り写真アカウント番号 */
	private AccountNo favoritePhotoAccountNo;

	/** お気に入り写真番号 */
	private PhotoNo favoritePhotoNo;

	/** 作成者 */
	private CreatedBy createdBy;

	/** 作成日時 */
	private CreatedAt createdAt;

	/**
	 * PhotoFavoriteModelからPhotoFavoriteエンティティを生成する
	 *
	 * @param	model	{@link PhotoFavoriteModel}
	 * @return			{@link PhotoFavorite}
	 */
	public static PhotoFavorite from(PhotoFavoriteModel model) {
		return PhotoFavorite.builder()
				.accountNo(model.getAccountNo())
				.favoritePhotoAccountNo(model.getFavoritePhotoAccountNo())
				.favoritePhotoNo(model.getFavoritePhotoNo())
				.createdBy(new CreatedBy(model.getAccountNo().value()))
				.build();
	}

	/**
	 * PhotoFavoriteDeleteModelからPhotoFavoriteエンティティを生成する
	 *
	 * @param	model	{@link PhotoFavoriteDeleteModel}
	 * @return			{@link PhotoFavorite}
	 */
	public static PhotoFavorite from(PhotoFavoriteDeleteModel model) {
		return PhotoFavorite.builder()
				.accountNo(model.getAccountNo())
				.favoritePhotoAccountNo(model.getFavoritePhotoAccountNo())
				.favoritePhotoNo(model.getFavoritePhotoNo())
				.build();
	}

	/**
	 * 写真お気に入り全件削除用のPhotoFavoriteDeleteModelからPhotoFavoriteエンティティを生成する
	 *
	 * @param	model	{@link PhotoFavoriteDeleteModel}
	 * @return			{@link PhotoFavorite}
	 */
	public static PhotoFavorite fromForClear(PhotoFavoriteDeleteModel model) {
		return PhotoFavorite.builder()
				.favoritePhotoAccountNo(model.getFavoritePhotoAccountNo())
				.favoritePhotoNo(model.getFavoritePhotoNo())
				.build();
	}
}
