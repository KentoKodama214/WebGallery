package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * 区分分類英語名の値オブジェクト
 *
 * @param	value	区分分類英語名
 */
public record KbnClassEnglishName(String value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public KbnClassEnglishName {
		if (value == null) {
			throw new IllegalArgumentException("区分分類英語名はnullにできません");
		}
	}
}
