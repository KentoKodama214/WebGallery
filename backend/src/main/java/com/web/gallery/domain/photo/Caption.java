package com.web.gallery.domain.photo;

import java.io.Serializable;

import com.web.gallery.constant.Consts;

/**
 * キャプションの値オブジェクト
 *
 * @param	value	キャプション
 */
public record Caption(String value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public Caption {
		if (value == null) {
			throw new IllegalArgumentException("キャプションはnullにできません");
		}
	}

	/**
	 * 未設定の場合にDB保存用のデフォルト値を返す
	 *
	 * @param	nullable	{@link Caption}（null許容）
	 * @return				nullableがnullでなければそのまま、nullであればデフォルト値を持つ{@link Caption}
	 */
	public static Caption getOrDefault(Caption nullable) {
		return nullable != null ? nullable : new Caption(Consts.STRING_EMPTY);
	}
}
