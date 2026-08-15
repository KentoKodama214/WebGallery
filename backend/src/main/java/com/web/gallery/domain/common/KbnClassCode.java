package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * 区分分類コードの値オブジェクト
 *
 * @param	value	区分分類コード
 */
public record KbnClassCode(String value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public KbnClassCode {
		if (value == null) {
			throw new IllegalArgumentException("区分分類コードはnullにできません");
		}
	}
}
