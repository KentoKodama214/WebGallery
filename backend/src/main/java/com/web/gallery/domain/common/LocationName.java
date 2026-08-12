package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * ロケーション名の値オブジェクト
 *
 * @param	value	ロケーション名
 */
public record LocationName(String value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public LocationName {
		if (value == null) {
			throw new IllegalArgumentException("ロケーション名はnullにできません");
		}
	}
}
