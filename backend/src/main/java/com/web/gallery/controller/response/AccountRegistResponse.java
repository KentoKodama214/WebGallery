package com.web.gallery.controller.response;

import org.springframework.http.HttpStatus;

import lombok.Builder;
import lombok.Data;

/**
 * アカウント登録のレスポンスパラメータを保持するクラス
 */
@Data
@Builder
public class AccountRegistResponse {
	/** HTTPステータス */
	private Integer httpStatus;

	/** 登録成功 */
	private Boolean isSuccess;

	/** メッセージ */
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