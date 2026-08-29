package com.web.gallery.domain.photo;

import java.io.Serializable;

import com.web.gallery.constant.Consts;

/**
 * 写真タイトル英語名の値オブジェクト
 *
 * @param	value	写真タイトル英語名
 */
public record PhotoEnglishTitle(String value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullまたは100文字を超える場合
	 */
	public PhotoEnglishTitle {
		if (value == null) {
			throw new IllegalArgumentException("写真タイトル英語名はnullにできません");
		}
		if (value.length() > 100) {
			throw new IllegalArgumentException("写真タイトル英語名は100文字以内である必要があります");
		}
	}

	/**
	 * 未設定の場合にDB保存用のデフォルト値を返す
	 *
	 * @param	nullable	{@link PhotoEnglishTitle}（null許容）
	 * @return				nullableがnullでなければそのまま、nullであればデフォルト値を持つ{@link PhotoEnglishTitle}
	 */
	public static PhotoEnglishTitle getOrDefault(PhotoEnglishTitle nullable) {
		return nullable != null ? nullable : new PhotoEnglishTitle(Consts.STRING_EMPTY);
	}
}
