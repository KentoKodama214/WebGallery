package com.web.gallery.domain.account;

import java.io.Serializable;
import java.util.Objects;

/**
 * アカウントIDの値オブジェクト
 */
public final class AccountId implements Serializable {
	private final String value;

	/**
	 * コンストラクタ
	 *
	 * @param	value	アカウントID
	 * @throws	IllegalArgumentException	nullまたはパターン不一致の場合
	 */
	public AccountId(String value) {
		if (value == null) {
			throw new IllegalArgumentException("アカウントIDはnullにできません");
		}
		if (!value.matches("^[a-zA-Z0-9]{8,16}$")) {
			throw new IllegalArgumentException("アカウントIDは半角英数字8〜16文字である必要があります");
		}
		this.value = value;
	}

	/**
	 * 値を取得する
	 *
	 * @return	アカウントID
	 */
	public String getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AccountId that = (AccountId) o;
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
