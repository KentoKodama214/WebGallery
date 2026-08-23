package com.web.gallery.entity;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.model.PhotoFavoriteDeleteModel;

import lombok.Builder;
import lombok.Data;

/**
 * 写真お気に入りテーブルの抽出条件クラス
 */
@Data
@Builder
public class PhotoFavoriteCondition {
	/** アカウント番号 */
	private AccountNo accountNo;

	/** お気に入り写真アカウント番号 */
	private AccountNo favoritePhotoAccountNo;

	/** お気に入り写真番号 */
	private PhotoNo favoritePhotoNo;

	/**
	 * PhotoFavoriteDeleteModelから抽出条件を生成する
	 *
	 * @param	model	{@link PhotoFavoriteDeleteModel}
	 * @return			{@link PhotoFavoriteCondition}
	 */
	public static PhotoFavoriteCondition from(PhotoFavoriteDeleteModel model) {
		return PhotoFavoriteCondition.builder()
				.accountNo(model.getAccountNo())
				.favoritePhotoAccountNo(model.getFavoritePhotoAccountNo())
				.favoritePhotoNo(model.getFavoritePhotoNo())
				.build();
	}

	/**
	 * 写真お気に入り全件削除用のPhotoFavoriteDeleteModelから抽出条件を生成する
	 *
	 * @param	model	{@link PhotoFavoriteDeleteModel}
	 * @return			{@link PhotoFavoriteCondition}
	 */
	public static PhotoFavoriteCondition forClear(PhotoFavoriteDeleteModel model) {
		return PhotoFavoriteCondition.builder()
				.favoritePhotoAccountNo(model.getFavoritePhotoAccountNo())
				.favoritePhotoNo(model.getFavoritePhotoNo())
				.build();
	}

	/**
	 * アカウント番号で自分が登録したお気に入り削除用の抽出条件を生成する
	 *
	 * @param	accountNo	アカウント番号
	 * @return				{@link PhotoFavoriteCondition}
	 */
	public static PhotoFavoriteCondition byAccountNo(AccountNo accountNo) {
		return PhotoFavoriteCondition.builder()
				.accountNo(accountNo)
				.build();
	}

	/**
	 * アカウント番号で自分の写真に対する他人のお気に入り削除用の抽出条件を生成する
	 *
	 * @param	favoritePhotoAccountNo	お気に入り写真アカウント番号
	 * @return							{@link PhotoFavoriteCondition}
	 */
	public static PhotoFavoriteCondition byFavoritePhotoAccountNo(AccountNo favoritePhotoAccountNo) {
		return PhotoFavoriteCondition.builder()
				.favoritePhotoAccountNo(favoritePhotoAccountNo)
				.build();
	}
}
