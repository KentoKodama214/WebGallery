package com.web.gallery.domain.common;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * 更新日時の値オブジェクト
 */
public final class UpdatedAt implements Serializable {
	private final OffsetDateTime value;

	/**
	 * コンストラクタ
	 *
	 * @param	value	更新日時
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public UpdatedAt(OffsetDateTime value) {
		if (value == null) {
			throw new IllegalArgumentException("更新日時はnullにできません");
		}
		this.value = value;
	}

	/**
	 * 値を取得する
	 *
	 * @return	更新日時
	 */
	public OffsetDateTime getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UpdatedAt that = (UpdatedAt) o;
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
