package com.web.gallery.domain.account;

import java.io.Serializable;
import java.util.Objects;

/**
 * パスワードの値オブジェクト
 */
public final class Password implements Serializable {
	private final String value;

	/**
	 * コンストラクタ
	 *
	 * @param	value	パスワード
	 * @throws	IllegalArgumentException	nullまたは空白の場合
	 */
	public Password(String value) {
		if (value == null) {
			throw new IllegalArgumentException("パスワードはnullにできません");
		}
		if (value.isBlank()) {
			throw new IllegalArgumentException("パスワードは空白にできません");
		}
		this.value = value;
	}

	/**
	 * 値を取得する
	 *
	 * @return	パスワード
	 */
	public String getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Password that = (Password) o;
		return Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public String toString() {
		return "****";
	}
}
