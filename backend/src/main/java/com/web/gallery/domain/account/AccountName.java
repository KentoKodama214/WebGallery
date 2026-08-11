package com.web.gallery.domain.account;

import java.io.Serializable;
import java.util.Objects;

/**
 * アカウント名の値オブジェクト
 */
public final class AccountName implements Serializable {
	private final String value;

	/**
	 * コンストラクタ
	 *
	 * @param	value	アカウント名
	 * @throws	IllegalArgumentException	nullまたは空白の場合
	 */
	public AccountName(String value) {
		if (value == null) {
			throw new IllegalArgumentException("アカウント名はnullにできません");
		}
		if (value.isBlank()) {
			throw new IllegalArgumentException("アカウント名は空白にできません");
		}
		this.value = value;
	}

	/**
	 * 値を取得する
	 *
	 * @return	アカウント名
	 */
	public String getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AccountName that = (AccountName) o;
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
