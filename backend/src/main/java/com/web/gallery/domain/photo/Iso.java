package com.web.gallery.domain.photo;

import java.io.Serializable;

/**
 * ISOの値オブジェクト
 *
 * @param	value	ISO
 */
public record Iso(Integer value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullまたは負の値の場合
	 */
	public Iso {
		if (value == null) {
			throw new IllegalArgumentException("ISOはnullにできません");
		}
		if (value < 0) {
			throw new IllegalArgumentException("ISOは0以上である必要があります");
		}
	}
}
