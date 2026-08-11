package com.web.gallery.domain.account;

import java.io.Serializable;
import java.util.Objects;

/**
 * フリーメモの値オブジェクト
 */
public final class FreeMemo implements Serializable {
	private final String value;

	/**
	 * コンストラクタ
	 * <p>
	 * null/空文字を許容する
	 *
	 * @param	value	フリーメモ
	 */
	public FreeMemo(String value) {
		this.value = value;
	}

	/**
	 * 値を取得する
	 *
	 * @return	フリーメモ
	 */
	public String getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FreeMemo that = (FreeMemo) o;
		return Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public String toString() {
		return value == null ? "" : value;
	}
}
