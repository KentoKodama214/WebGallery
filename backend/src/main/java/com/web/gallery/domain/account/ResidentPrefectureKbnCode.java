package com.web.gallery.domain.account;

import java.io.Serializable;

/**
 * 在住都道府県区分コードの値オブジェクト
 *
 * @param	value	在住都道府県区分コード
 */
public record ResidentPrefectureKbnCode(String value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public ResidentPrefectureKbnCode {
		if (value == null) {
			throw new IllegalArgumentException("在住都道府県区分コードはnullにできません");
		}
	}
}
