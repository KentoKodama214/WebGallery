package com.web.gallery.domain.photo;

import java.io.Serializable;

/**
 * 写真番号の値オブジェクト
 *
 * @param	value	写真番号
 */
public record PhotoNo(Long value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullまたは0以下の場合
	 */
	public PhotoNo {
		if (value == null) {
			throw new IllegalArgumentException("写真番号はnullにできません");
		}
		if (value <= 0) {
			throw new IllegalArgumentException("写真番号は正の値である必要があります");
		}
	}
}
