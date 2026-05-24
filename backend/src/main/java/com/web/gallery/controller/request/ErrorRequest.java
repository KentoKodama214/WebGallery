package com.web.gallery.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * エラーページ表示時のリクエストパラメータを保持するクラス
 */
@Schema(description = "エラーレスポンス")
@Data
@Builder
public class ErrorRequest {
	/** HTTPステータス */
	@Schema(description = "HTTPステータスコード", example = "400")
	private Integer httpStatus;

	/** エラーコード */
	@Schema(description = "エラーコード", example = "E-C-0001")
	private String errorCode;

	/** エラーメッセージ */
	@Schema(description = "エラーメッセージ", example = "入力内容が不正です")
	private String errorMessage;
}