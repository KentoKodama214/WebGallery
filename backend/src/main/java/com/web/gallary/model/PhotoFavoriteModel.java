package com.web.gallary.model;

import com.web.gallary.controller.request.PhotoFavoriteDeleteRequest;
import com.web.gallary.controller.request.PhotoFavoriteRegistRequest;

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
	private Integer accountNo;
	
	/** お気に入り写真アカウント番号 */
	@NonNull
	private Integer favoritePhotoAccountNo;
	
	/** 写真番号 */
	@NonNull
	private Integer favoritePhotoNo;

	/**
	 * お気に入り登録リクエストからPhotoFavoriteModelを生成する
	 *
	 * @param	request		{@link PhotoFavoriteRegistRequest}
	 * @param	accountNo	アカウント番号
	 * @return				{@link PhotoFavoriteModel}
	 */
	public static PhotoFavoriteModel from(PhotoFavoriteRegistRequest request, Integer accountNo) {
		return PhotoFavoriteModel.builder()
				.accountNo(accountNo)
				.favoritePhotoAccountNo(request.getFavoritePhotoAccountNo())
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
	public static PhotoFavoriteModel from(PhotoFavoriteDeleteRequest request, Integer accountNo) {
		return PhotoFavoriteModel.builder()
				.accountNo(accountNo)
				.favoritePhotoAccountNo(request.getFavoritePhotoAccountNo())
				.favoritePhotoNo(request.getFavoritePhotoNo())
				.build();
	}
}