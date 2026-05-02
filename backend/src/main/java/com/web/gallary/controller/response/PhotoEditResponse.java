package com.web.gallary.controller.response;

import lombok.Builder;
import lombok.Data;

/**
 * 写真登録・編集のレスポンスパラメータを保持するクラス
 */
@Data
@Builder
public class PhotoEditResponse {
	/** HTTPステータス */
	private Integer httpStatus;

	/** 登録成功 */
	private Boolean isSuccess;
	
	/** メッセージ */
	private String message;
}