package com.web.gallery.domain.photo;

import java.io.Serializable;

/**
 * 焦点距離の値オブジェクト
 *
 * @param	value	焦点距離
 */
public record FocalLength(Integer value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullまたは負の値の場合
	 */
	public FocalLength {
		if (value == null) {
			throw new IllegalArgumentException("焦点距離はnullにできません");
		}
		if (value < 0) {
			throw new IllegalArgumentException("焦点距離は0以上である必要があります");
		}
	}
}
