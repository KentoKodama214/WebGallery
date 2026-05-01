package com.web.gallary.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * 写真の情報を取得する時の情報を受け渡すためのModelクラス
 */
@Value
@Builder
public class PhotoGetModel {
	/** ログイン中のアカウントNo */
	private Integer accountNo;
	
	/** 写真のアカウントNo */
	@NonNull
	private Integer photoAccountNo;
}