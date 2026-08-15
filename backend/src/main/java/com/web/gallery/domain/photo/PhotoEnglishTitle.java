package com.web.gallery.domain.photo;

import java.io.Serializable;

/**
 * 写真タイトル英語名の値オブジェクト
 *
 * @param	value	写真タイトル英語名
 */
public record PhotoEnglishTitle(String value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public PhotoEnglishTitle {
		if (value == null) {
			throw new IllegalArgumentException("写真タイトル英語名はnullにできません");
		}
	}
}
