package com.web.gallery.domain.account;

import java.io.Serializable;
import java.util.Objects;

/**
 * 在住都道府県区分コードの値オブジェクト
 */
public final class ResidentPrefectureKbnCode implements Serializable {
	private final String value;

	/**
	 * コンストラクタ
	 *
	 * @param	value	在住都道府県区分コード
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public ResidentPrefectureKbnCode(String value) {
		if (value == null) {
			throw new IllegalArgumentException("在住都道府県区分コードはnullにできません");
		}
		this.value = value;
	}

	/**
	 * 値を取得する
	 *
	 * @return	在住都道府県区分コード
	 */
	public String getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResidentPrefectureKbnCode that = (ResidentPrefectureKbnCode) o;
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
