package com.web.gallery.domain.account;

import java.io.Serializable;

import com.web.gallery.constant.Consts;

/**
 * 出身都道府県区分コードの値オブジェクト
 *
 * @param	value	出身都道府県区分コード
 */
public record BirthplacePrefectureKbnCode(String value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public BirthplacePrefectureKbnCode {
		if (value == null) {
			throw new IllegalArgumentException("出身都道府県区分コードはnullにできません");
		}
	}

	/**
	 * 未設定の場合にDB保存用のデフォルト値を返す
	 *
	 * @param	nullable	{@link BirthplacePrefectureKbnCode}（null許容）
	 * @return				nullableがnullでなければそのまま、nullであればデフォルト値を持つ{@link BirthplacePrefectureKbnCode}
	 */
	public static BirthplacePrefectureKbnCode getOrDefault(BirthplacePrefectureKbnCode nullable) {
		return nullable != null ? nullable : new BirthplacePrefectureKbnCode(Consts.STRING_NONE);
	}
}
