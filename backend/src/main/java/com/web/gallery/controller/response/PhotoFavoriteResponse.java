package com.web.gallery.controller.response;

import org.springframework.http.HttpStatus;

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

	/**
	 * 成功レスポンスを生成する
	 *
	 * @param	message	メッセージ
	 * @return			{@link PhotoFavoriteResponse}
	 */
	public static PhotoFavoriteResponse of(String message) {
		return PhotoFavoriteResponse.builder()
				.httpStatus(HttpStatus.OK.value())
				.isSuccess(true)
				.message(message)
				.build();
	}
}