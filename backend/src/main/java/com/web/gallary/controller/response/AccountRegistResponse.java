package com.web.gallary.controller.response;

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
}