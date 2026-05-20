package com.web.gallery.model;

import com.web.gallery.entity.KbnMst;

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

	/**
	 * KbnMstエンティティからKbnMstModelを生成する
	 *
	 * @param	entity	{@link KbnMst}
	 * @return			{@link KbnMstModel}
	 */
	public static KbnMstModel from(KbnMst entity) {
		return KbnMstModel.builder()
				.kbnClassCode(entity.getKbnClassCode())
				.kbnCode(entity.getKbnCode())
				.sortOrder(entity.getSortOrder())
				.kbnGroupCode(entity.getKbnGroupCode())
				.kbnClassJapaneseName(entity.getKbnClassJapaneseName())
				.kbnGroupJapaneseName(entity.getKbnGroupJapaneseName())
				.kbnJapaneseName(entity.getKbnJapaneseName())
				.kbnClassEnglishName(entity.getKbnClassEnglishName())
				.kbnGroupEnglishName(entity.getKbnGroupEnglishName())
				.kbnEnglishName(entity.getKbnEnglishName())
				.explanation(entity.getExplanation())
				.build();
	}
}