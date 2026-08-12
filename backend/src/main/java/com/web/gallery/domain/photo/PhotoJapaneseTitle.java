package com.web.gallery.domain.photo;

import java.io.Serializable;

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
}
