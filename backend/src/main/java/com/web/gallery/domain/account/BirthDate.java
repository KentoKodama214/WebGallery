package com.web.gallery.domain.account;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 生年月日の値オブジェクト
 *
 * @param	value	生年月日
 */
public record BirthDate(LocalDate value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public BirthDate {
		if (value == null) {
			throw new IllegalArgumentException("生年月日はnullにできません");
		}
	}
}
