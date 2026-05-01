package com.web.gallary.model;

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
	private Integer accountNo;
	
	/** お気に入り写真アカウント番号 */
	@NonNull
	private Integer favoritePhotoAccountNo;
	
	/** 写真番号 */
	@NonNull
	private Integer favoritePhotoNo;
}