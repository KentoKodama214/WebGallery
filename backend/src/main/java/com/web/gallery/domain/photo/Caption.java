package com.web.gallery.domain.photo;

import java.io.Serializable;

/**
 * キャプションの値オブジェクト
 *
 * @param	value	キャプション
 */
public record Caption(String value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public Caption {
		if (value == null) {
			throw new IllegalArgumentException("キャプションはnullにできません");
		}
	}
}
