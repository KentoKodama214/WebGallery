package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * 削除フラグの値オブジェクト
 *
 * @param	value	削除フラグ
 */
public record IsDeleted(Boolean value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public IsDeleted {
		if (value == null) {
			throw new IllegalArgumentException("削除フラグはnullにできません");
		}
	}
}
