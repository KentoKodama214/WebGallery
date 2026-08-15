package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * 区分コードの値オブジェクト
 *
 * @param	value	区分コード
 */
public record KbnCode(String value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public KbnCode {
		if (value == null) {
			throw new IllegalArgumentException("区分コードはnullにできません");
		}
	}
}
