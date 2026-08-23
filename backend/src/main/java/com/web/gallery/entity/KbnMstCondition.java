package com.web.gallery.entity;

import com.web.gallery.domain.common.Explanation;
import com.web.gallery.domain.common.KbnClassCode;
import com.web.gallery.domain.common.KbnClassEnglishName;
import com.web.gallery.domain.common.KbnClassJapaneseName;
import com.web.gallery.domain.common.KbnCode;
import com.web.gallery.domain.common.KbnEnglishName;
import com.web.gallery.domain.common.KbnGroupCode;
import com.web.gallery.domain.common.KbnGroupEnglishName;
import com.web.gallery.domain.common.KbnGroupJapaneseName;
import com.web.gallery.domain.common.KbnJapaneseName;
import com.web.gallery.domain.common.SortOrder;

import lombok.Builder;
import lombok.Data;

/**
 * 区分マスタテーブルの抽出条件クラス
 */
@Data
@Builder
public class KbnMstCondition {
	/** 区分分類コード */
	private KbnClassCode kbnClassCode;

	/** 区分コード */
	private KbnCode kbnCode;

	/** 並び順 */
	private SortOrder sortOrder;

	/** 区分グループコード */
	private KbnGroupCode kbnGroupCode;

	/** 区分分類日本語名 */
	private KbnClassJapaneseName kbnClassJapaneseName;

	/** 区分グループ日本語名 */
	private KbnGroupJapaneseName kbnGroupJapaneseName;

	/** 区分日本語名 */
	private KbnJapaneseName kbnJapaneseName;

	/** 区分分類英語名 */
	private KbnClassEnglishName kbnClassEnglishName;

	/** 区分グループ英語名 */
	private KbnGroupEnglishName kbnGroupEnglishName;

	/** 区分英語名 */
	private KbnEnglishName kbnEnglishName;

	/** 説明 */
	private Explanation explanation;

	/**
	 * 区分分類コードによる抽出条件を生成する
	 *
	 * @param	kbnClassCode	区分分類コード
	 * @return					{@link KbnMstCondition}
	 */
	public static KbnMstCondition byKbnClassCode(String kbnClassCode) {
		return KbnMstCondition.builder()
				.kbnClassCode(new KbnClassCode(kbnClassCode))
				.build();
	}
}
