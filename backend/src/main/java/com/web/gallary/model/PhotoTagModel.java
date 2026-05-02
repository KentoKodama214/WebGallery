package com.web.gallary.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * 写真タグの情報を受け渡すためのModelクラス
 */
@Value
@Builder
public class PhotoTagModel {
	/** アカウント番号 */
	@NonNull
	private Integer accountNo;

	/** 写真番号 */
	private Integer photoNo;

	/** タグ番号 */
	private Integer tagNo;

	/** タグ日本語名 */
	@NonNull
	private String tagJapaneseName;

	/** タグ英語名 */
	@NonNull
	private String tagEnglishName;
}