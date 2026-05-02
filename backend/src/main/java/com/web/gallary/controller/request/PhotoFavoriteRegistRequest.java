package com.web.gallary.controller.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 写真お気に入り登録時のリクエストパラメータを保持するクラス
 */
@Data
public class PhotoFavoriteRegistRequest {
	/** お気に入り写真アカウント番号 */
	@NotNull(message = "{validation.common.notBlank}")
	@Positive(message = "{validation.common.positive}")
	private Integer favoritePhotoAccountNo;
	
	/** お気に入り写真番号 */
	@NotNull(message = "{validation.common.notBlank}")
	@Positive(message = "{validation.common.positive}")
	private Integer favoritePhotoNo;
}