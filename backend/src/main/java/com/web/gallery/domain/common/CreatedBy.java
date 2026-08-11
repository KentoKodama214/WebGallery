package com.web.gallery.domain.common;

import java.io.Serializable;
import java.util.Objects;

/**
 * 作成者の値オブジェクト
 */
public final class CreatedBy implements Serializable {
	private final Long value;

	/**
	 * コンストラクタ
	 *
	 * @param	value	作成者
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public CreatedBy(Long value) {
		if (value == null) {
			throw new IllegalArgumentException("作成者はnullにできません");
		}
		this.value = value;
	}

	/**
	 * 値を取得する
	 *
	 * @return	作成者
	 */
	public Long getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CreatedBy that = (CreatedBy) o;
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
