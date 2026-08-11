package com.web.gallery.domain.account;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * 生年月日の値オブジェクト
 */
public final class BirthDate implements Serializable {
	private final LocalDate value;

	/**
	 * コンストラクタ
	 *
	 * @param	value	生年月日
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public BirthDate(LocalDate value) {
		if (value == null) {
			throw new IllegalArgumentException("生年月日はnullにできません");
		}
		this.value = value;
	}

	/**
	 * 値を取得する
	 *
	 * @return	生年月日
	 */
	public LocalDate getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BirthDate that = (BirthDate) o;
		return Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
