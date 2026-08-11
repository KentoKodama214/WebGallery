package com.web.gallery.model;

import com.web.gallery.domain.account.AccountNo;

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
	private Long favoritePhotoNo;
}
