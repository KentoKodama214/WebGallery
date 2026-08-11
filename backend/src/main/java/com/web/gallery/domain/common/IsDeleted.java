package com.web.gallery.domain.common;

import java.io.Serializable;
import java.util.Objects;

/**
 * 削除フラグの値オブジェクト
 */
public final class IsDeleted implements Serializable {
	private final Boolean value;

	/**
	 * コンストラクタ
	 *
	 * @param	value	削除フラグ
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public IsDeleted(Boolean value) {
		if (value == null) {
			throw new IllegalArgumentException("削除フラグはnullにできません");
		}
		this.value = value;
	}

	/**
	 * 値を取得する
	 *
	 * @return	削除フラグ
	 */
	public Boolean getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		IsDeleted that = (IsDeleted) o;
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
