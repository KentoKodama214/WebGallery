package com.web.gallary.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * 写真のメタデータを含めた詳細情報を取得するときの情報を受け渡すためのModelクラス
 */
@Value
@Builder
public class PhotoDetailGetModel {
	/** ログイン中のアカウントNo */
	private Integer accountNo;
	
	/** 写真のアカウントNo */
	@NonNull
	private Integer photoAccountNo;
	
	/** 写真番号 */
	@NonNull
	private Integer photoNo;
}