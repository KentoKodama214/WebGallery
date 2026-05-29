package com.web.gallery.controller.response;

import org.springframework.http.HttpStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 管理者用アカウントロック操作のレスポンスパラメータを保持するクラス
 */
@Schema(description = "管理者用アカウントロック操作レスポンス")
@Data
@Builder
public class AdminAccountLockResponse {
	/** HTTPステータス */
	@Schema(description = "HTTPステータスコード", example = "200")
	private Integer httpStatus;

	/** 成功フラグ */
	@Schema(description = "成功", example = "true")
	private Boolean isSuccess;

	/** メッセージ */
	@Schema(description = "メッセージ")
	private String message;

	/**
	 * 成功レスポンスを生成する
	 *
	 * @param	message	メッセージ
	 * @return			{@link AdminAccountLockResponse}
	 */
	public static AdminAccountLockResponse of(String message) {
		return AdminAccountLockResponse.builder()
				.httpStatus(HttpStatus.OK.value())
				.isSuccess(true)
				.message(message)
				.build();
	}
}
