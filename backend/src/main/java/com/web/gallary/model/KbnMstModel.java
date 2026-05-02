package com.web.gallary.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * 区分マスタの情報を受け渡すためのModelクラス
 */
@Value
@Builder
public class KbnMstModel {
	/** 区分分類コード */
	@NonNull
	private String kbnClassCode;
	
	/** 区分コード */
	@NonNull
	private String kbnCode;
	
	/** 並び順 */
	@NonNull
	private Integer sortOrder;
	
	/** 区分グループコード */
	@NonNull
	private String kbnGroupCode;
	
	/** 区分分類日本語名 */
	@NonNull
	private String kbnClassJapaneseName;
	
	/** 区分グループ日本語名 */
	@NonNull
	private String kbnGroupJapaneseName;
	
	/** 区分日本語名 */
	@NonNull
	private String kbnJapaneseName;

	/** 区分分類英語名 */
	@NonNull
	private String kbnClassEnglishName;
	
	/** 区分グループ英語名 */
	@NonNull
	private String kbnGroupEnglishName;
	
	/** 区分英語名 */
	@NonNull
	private String kbnEnglishName;
	
	/** 説明 */
	@NonNull
	private String explanation;
}