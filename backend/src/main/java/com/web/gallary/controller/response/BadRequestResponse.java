package com.web.gallary.controller.response;

import org.springframework.http.HttpStatus;

import com.web.gallary.exception.BadRequestException;

import lombok.Builder;
import lombok.Data;

/**
 * パラメータ不正の時のレスポンスパラメータを保持するクラス
 */
@Data
@Builder
public class BadRequestResponse {
	/** HTTPステータス */
	private Integer httpStatus;

	/** 登録成功 */
	private Boolean isSuccess;

	/** メッセージ */
	private String message;

	/**
	 * BadRequestExceptionからエラーレスポンスを生成する
	 *
	 * @param	exception	{@link BadRequestException}
	 * @return				{@link BadRequestResponse}
	 */
	public static BadRequestResponse of(BadRequestException exception) {
		return BadRequestResponse.builder()
				.httpStatus(HttpStatus.BAD_REQUEST.value())
				.isSuccess(false)
				.message(exception.getMessage())
				.build();
	}
}