package com.web.gallery.controller.response;

import org.springframework.http.HttpStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * アカウント登録のレスポンスパラメータを保持するクラス
 */
@Schema(description = "アカウント登録レスポンス")
@Data
@Builder
public class AccountRegistResponse {
	/** HTTPステータス */
	@Schema(description = "HTTPステータスコード", example = "200")
	private Integer httpStatus;

	/** 登録成功 */
	@Schema(description = "登録成功", example = "true")
	private Boolean isSuccess;

	/** メッセージ */
	@Schema(description = "メッセージ")
	private String message;

	/**
	 * 成功レスポンスを生成する
	 *
	 * @param	isSuccess	登録成功
	 * @param	message		メッセージ
	 * @return				{@link AccountRegistResponse}
	 */
	public static AccountRegistResponse of(Boolean isSuccess, String message) {
		return AccountRegistResponse.builder()
				.httpStatus(HttpStatus.OK.value())
				.isSuccess(isSuccess)
				.message(message)
				.build();
	}
}