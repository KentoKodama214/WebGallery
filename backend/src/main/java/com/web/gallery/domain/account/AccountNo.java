package com.web.gallery.domain.account;

import java.io.Serializable;
import java.util.Objects;

/**
 * アカウント番号の値オブジェクト
 */
public final class AccountNo implements Serializable {
	private final Long value;

	/**
	 * コンストラクタ
	 *
	 * @param	value	アカウント番号
	 * @throws	IllegalArgumentException	nullまたは0以下の場合
	 */
	public AccountNo(Long value) {
		if (value == null) {
			throw new IllegalArgumentException("アカウント番号はnullにできません");
		}
		if (value <= 0) {
			throw new IllegalArgumentException("アカウント番号は正の値である必要があります");
		}
		this.value = value;
	}

	/**
	 * 値を取得する
	 *
	 * @return	アカウント番号
	 */
	public Long getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AccountNo that = (AccountNo) o;
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
