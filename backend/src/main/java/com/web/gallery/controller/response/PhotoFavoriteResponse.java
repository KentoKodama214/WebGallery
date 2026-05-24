package com.web.gallery.controller.response;

import org.springframework.http.HttpStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 写真お気に入り登録/解除のレスポンスパラメータを保持するクラス
 */
@Schema(description = "お気に入り操作レスポンス")
@Data
@Builder
public class PhotoFavoriteResponse {
	/** HTTPステータス */
	@Schema(description = "HTTPステータスコード", example = "200")
	private Integer httpStatus;

	/** 登録成功 */
	@Schema(description = "成功", example = "true")
	private Boolean isSuccess;

	/** メッセージ */
	@Schema(description = "メッセージ")
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