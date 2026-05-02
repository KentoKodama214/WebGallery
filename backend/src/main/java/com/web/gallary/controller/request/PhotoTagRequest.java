package com.web.gallary.controller.request;

import lombok.Data;

/**
 * 写真登録・編集ページ表示時のタグ表示のリクエストパラメータを保持するクラス
 */
@Data
public class PhotoTagRequest {
	/** アカウント番号 */
	private Integer accountNo;

	/** 写真番号 */
	private Integer photoNo;

	/** タグ番号 */
	private Integer tagNo;

	/** タグ日本語名 */
	private String tagJapaneseName;

	/** タグ英語名 */
	private String tagEnglishName;
}