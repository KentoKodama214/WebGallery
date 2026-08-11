package com.web.gallery.domain.account;

import java.io.Serializable;
import java.util.Objects;

/**
 * 出身都道府県区分コードの値オブジェクト
 */
public final class BirthplacePrefectureKbnCode implements Serializable {
	private final String value;

	/**
	 * コンストラクタ
	 *
	 * @param	value	出身都道府県区分コード
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public BirthplacePrefectureKbnCode(String value) {
		if (value == null) {
			throw new IllegalArgumentException("出身都道府県区分コードはnullにできません");
		}
		this.value = value;
	}

	/**
	 * 値を取得する
	 *
	 * @return	出身都道府県区分コード
	 */
	public String getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BirthplacePrefectureKbnCode that = (BirthplacePrefectureKbnCode) o;
		return Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public String toString() {
		return value;
	}
}
