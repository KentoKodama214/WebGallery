package com.web.gallary.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * 写真タグを削除するときの情報を受け渡すためのModelクラス
 */
@Value
@Builder
public class PhotoTagDeleteModel {
	/** アカウント番号 */
	@NonNull
	private Integer accountNo;

	/** 写真番号 */
	@NonNull
	private Integer photoNo;
}