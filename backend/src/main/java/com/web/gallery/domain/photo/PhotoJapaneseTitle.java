package com.web.gallery.domain.photo;

import java.io.Serializable;

import com.web.gallery.constant.Consts;

/**
 * 写真タイトル日本語名の値オブジェクト
 *
 * @param	value	写真タイトル日本語名
 */
public record PhotoJapaneseTitle(String value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public PhotoJapaneseTitle {
		if (value == null) {
			throw new IllegalArgumentException("写真タイトル日本語名はnullにできません");
		}
	}

	/**
	 * 未設定の場合にDB保存用のデフォルト値を返す
	 *
	 * @param	nullable	{@link PhotoJapaneseTitle}（null許容）
	 * @return				nullableがnullでなければそのまま、nullであればデフォルト値を持つ{@link PhotoJapaneseTitle}
	 */
	public static PhotoJapaneseTitle getOrDefault(PhotoJapaneseTitle nullable) {
		return nullable != null ? nullable : new PhotoJapaneseTitle(Consts.STRING_EMPTY);
	}
}
