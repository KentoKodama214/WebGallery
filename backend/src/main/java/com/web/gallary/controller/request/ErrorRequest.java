package com.web.gallary.controller.request;

import lombok.Builder;
import lombok.Data;

/**
 * エラーページ表示時のリクエストパラメータを保持するクラス
 */
@Data
@Builder
public class ErrorRequest {
	/** HTTPステータス */
	private Integer httpStatus;
	
	/** エラーコード */
	private String errorCode;
	
	/** エラーメッセージ */
	private String errorMessage;
}