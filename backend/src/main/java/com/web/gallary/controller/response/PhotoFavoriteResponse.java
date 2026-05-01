package com.web.gallary.controller.response;

import lombok.Builder;
import lombok.Data;

/**
 * 写真お気に入り登録/解除のレスポンスパラメータを保持するクラス
 */
@Data
@Builder
public class PhotoFavoriteResponse {
	/** HTTPステータス */
	private Integer httpStatus;

	/** 登録成功 */
	private Boolean isSuccess;
	
	/** メッセージ */
	private String message;
}