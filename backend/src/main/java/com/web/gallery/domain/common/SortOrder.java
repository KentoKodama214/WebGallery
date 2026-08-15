package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * 並び順の値オブジェクト
 *
 * @param	value	並び順
 */
public record SortOrder(Integer value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public SortOrder {
		if (value == null) {
			throw new IllegalArgumentException("並び順はnullにできません");
		}
	}
}
