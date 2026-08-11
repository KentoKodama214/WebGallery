package com.web.gallery.domain.common;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * 作成日時の値オブジェクト
 */
public final class CreatedAt implements Serializable {
	private final OffsetDateTime value;

	/**
	 * コンストラクタ
	 *
	 * @param	value	作成日時
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public CreatedAt(OffsetDateTime value) {
		if (value == null) {
			throw new IllegalArgumentException("作成日時はnullにできません");
		}
		this.value = value;
	}

	/**
	 * 値を取得する
	 *
	 * @return	作成日時
	 */
	public OffsetDateTime getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CreatedAt that = (CreatedAt) o;
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
