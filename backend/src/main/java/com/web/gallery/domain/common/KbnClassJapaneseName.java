package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * 区分分類日本語名の値オブジェクト
 *
 * @param	value	区分分類日本語名
 */
public record KbnClassJapaneseName(String value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public KbnClassJapaneseName {
		if (value == null) {
			throw new IllegalArgumentException("区分分類日本語名はnullにできません");
		}
	}
}
