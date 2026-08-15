package com.web.gallery.domain.photo;

import java.io.Serializable;

/**
 * ロケーション番号の値オブジェクト
 *
 * @param	value	ロケーション番号
 */
public record LocationNo(Long value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullまたは負の値の場合
	 */
	public LocationNo {
		if (value == null) {
			throw new IllegalArgumentException("ロケーション番号はnullにできません");
		}
		if (value < 0) {
			throw new IllegalArgumentException("ロケーション番号は0以上である必要があります");
		}
	}
}
