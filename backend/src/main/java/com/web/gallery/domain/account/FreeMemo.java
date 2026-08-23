package com.web.gallery.domain.account;

import java.io.Serializable;

import com.web.gallery.constant.Consts;

/**
 * フリーメモの値オブジェクト
 * <p>
 * null/空文字を許容する
 *
 * @param	value	フリーメモ
 */
public record FreeMemo(String value) implements Serializable {
	@Override
	public String toString() {
		return value == null ? "" : value;
	}

	/**
	 * 未設定の場合にDB保存用のデフォルト値を返す
	 *
	 * @param	nullable	{@link FreeMemo}（null許容）
	 * @return				nullableがnullでなければそのまま、nullであればデフォルト値を持つ{@link FreeMemo}
	 */
	public static FreeMemo getOrDefault(FreeMemo nullable) {
		return nullable != null ? nullable : new FreeMemo(Consts.STRING_EMPTY);
	}
}
