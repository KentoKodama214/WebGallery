package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * 無効化フラグの値オブジェクト
 *
 * @param	value	無効化フラグ
 */
public record IsRevoked(Boolean value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public IsRevoked {
		if (value == null) {
			throw new IllegalArgumentException("無効化フラグはnullにできません");
		}
	}
}
