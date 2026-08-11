package com.web.gallery.domain.common;

import java.io.Serializable;
import java.util.Objects;

/**
 * 更新者の値オブジェクト
 */
public final class UpdatedBy implements Serializable {
	private final Long value;

	/**
	 * コンストラクタ
	 *
	 * @param	value	更新者
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public UpdatedBy(Long value) {
		if (value == null) {
			throw new IllegalArgumentException("更新者はnullにできません");
		}
		this.value = value;
	}

	/**
	 * 値を取得する
	 *
	 * @return	更新者
	 */
	public Long getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UpdatedBy that = (UpdatedBy) o;
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
