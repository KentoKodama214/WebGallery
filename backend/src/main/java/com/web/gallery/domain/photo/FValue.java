package com.web.gallery.domain.photo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * F値の値オブジェクト
 *
 * @param	value	F値
 */
public record FValue(BigDecimal value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullまたは負の値の場合
	 */
	public FValue {
		if (value == null) {
			throw new IllegalArgumentException("F値はnullにできません");
		}
		if (value.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("F値は0以上である必要があります");
		}
	}
}
