package com.web.gallery.model;

import com.web.gallery.controller.request.PhotoFavoriteDeleteRequest;
import com.web.gallery.controller.request.PhotoFavoriteRegistRequest;
import com.web.gallery.domain.account.AccountNo;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * 写真お気に入りの情報を受け渡すためのModelクラス
 */
@Value
@Builder
public class PhotoFavoriteModel {
	/** アカウント番号 */
	@NonNull
	private AccountNo accountNo;

	/** お気に入り写真アカウント番号 */
	@NonNull
	private AccountNo favoritePhotoAccountNo;

	/** 写真番号 */
	@NonNull
	private Long favoritePhotoNo;

	/**
	 * お気に入り登録リクエストからPhotoFavoriteModelを生成する
	 *
	 * @param	request		{@link PhotoFavoriteRegistRequest}
	 * @param	accountNo	アカウント番号
	 * @return				{@link PhotoFavoriteModel}
	 */
	public static PhotoFavoriteModel from(PhotoFavoriteRegistRequest request, Long accountNo) {
		return PhotoFavoriteModel.builder()
				.accountNo(new AccountNo(accountNo))
				.favoritePhotoAccountNo(new AccountNo(request.getFavoritePhotoAccountNo()))
				.favoritePhotoNo(request.getFavoritePhotoNo())
				.build();
	}

	/**
	 * お気に入り解除リクエストからPhotoFavoriteModelを生成する
	 *
	 * @param	request		{@link PhotoFavoriteDeleteRequest}
	 * @param	accountNo	アカウント番号
	 * @return				{@link PhotoFavoriteModel}
	 */
	public static PhotoFavoriteModel from(PhotoFavoriteDeleteRequest request, Long accountNo) {
		return PhotoFavoriteModel.builder()
				.accountNo(new AccountNo(accountNo))
				.favoritePhotoAccountNo(new AccountNo(request.getFavoritePhotoAccountNo()))
				.favoritePhotoNo(request.getFavoritePhotoNo())
				.build();
	}
}
