package com.web.gallary.entity;

import java.time.OffsetDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * 写真タグマスタテーブルのEntityクラス
 */
@Data
@Builder
public class PhotoTagMst {
	/** ID */
	private Integer id;

	/** アカウント番号 */
	private Integer accountNo;

	/** 写真番号 */
	private Integer photoNo;

	/** タグ番号 */
	private Integer tagNo;

	/** 作成者 */
	private Integer createdBy;

	/** 作成日時 */
	private OffsetDateTime createdAt;

	/** タグ日本語名 */
	private String tagJapaneseName;

	/** タグ英語名 */
	private String tagEnglishName;
}