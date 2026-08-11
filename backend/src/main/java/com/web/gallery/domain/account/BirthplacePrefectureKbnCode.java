package com.web.gallery.domain.account;

import java.io.Serializable;

/**
 * 出身都道府県区分コードの値オブジェクト
 *
 * @param	value	出身都道府県区分コード
 */
public record BirthplacePrefectureKbnCode(String value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public BirthplacePrefectureKbnCode {
		if (value == null) {
			throw new IllegalArgumentException("出身都道府県区分コードはnullにできません");
		}
	}
}
