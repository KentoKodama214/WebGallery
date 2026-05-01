package com.web.gallary.entity;

import java.time.OffsetDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * 区分マスタテーブルのEntityクラス
 */
@Data
@Builder
public class KbnMst {
	/** 区分分類コード */
	private String kbnClassCode;

	/** 区分コード */
	private String kbnCode;

	/** 作成者 */
	private Integer createdBy;

	/** 作成日時 */
	private OffsetDateTime createdAt;

	/** 並び順 */
	private Integer sortOrder;

	/** 区分グループコード */
	private String kbnGroupCode;
	
	/** 区分分類日本語名 */
	private String kbnClassJapaneseName;

	/** 区分グループ日本語名 */
	private String kbnGroupJapaneseName;
	
	/** 区分日本語名 */
	private String kbnJapaneseName;

	/** 区分分類英語名 */
	private String kbnClassEnglishName;

	/** 区分グループ英語名 */
	private String kbnGroupEnglishName;
	
	/** 区分英語名 */
	private String kbnEnglishName;

	/** 説明 */
	private String explanation;
}