package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * 説明の値オブジェクト
 *
 * @param	value	説明
 */
public record Explanation(String value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public Explanation {
		if (value == null) {
			throw new IllegalArgumentException("説明はnullにできません");
		}
	}
}
