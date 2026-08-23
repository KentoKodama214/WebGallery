package com.web.gallery.domain.account;

import java.io.Serializable;

import com.web.gallery.constant.Consts;

/**
 * 在住都道府県区分コードの値オブジェクト
 *
 * @param	value	在住都道府県区分コード
 */
public record ResidentPrefectureKbnCode(String value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public ResidentPrefectureKbnCode {
		if (value == null) {
			throw new IllegalArgumentException("在住都道府県区分コードはnullにできません");
		}
	}

	/**
	 * 未設定の場合にDB保存用のデフォルト値を返す
	 *
	 * @param	nullable	{@link ResidentPrefectureKbnCode}（null許容）
	 * @return				nullableがnullでなければそのまま、nullであればデフォルト値を持つ{@link ResidentPrefectureKbnCode}
	 */
	public static ResidentPrefectureKbnCode getOrDefault(ResidentPrefectureKbnCode nullable) {
		return nullable != null ? nullable : new ResidentPrefectureKbnCode(Consts.STRING_NONE);
	}
}
