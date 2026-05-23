package com.web.gallery.entity;

import java.time.OffsetDateTime;

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
	private Long accountNo;

	/** お気に入り写真アカウント番号 */
	private Long favoritePhotoAccountNo;

	/** お気に入り写真番号 */
	private Long favoritePhotoNo;

	/** 作成者 */
	private Long createdBy;

	/** 作成日時 */
	private OffsetDateTime createdAt;

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
				.createdBy(model.getAccountNo())
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