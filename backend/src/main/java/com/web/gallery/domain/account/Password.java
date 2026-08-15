package com.web.gallery.domain.account;

import java.io.Serializable;

/**
 * パスワードの値オブジェクト
 *
 * @param	value	パスワード
 */
public record Password(String value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullまたは空白の場合
	 */
	public Password {
		if (value == null) {
			throw new IllegalArgumentException("パスワードはnullにできません");
		}
		if (value.isBlank()) {
			throw new IllegalArgumentException("パスワードは空白にできません");
		}
	}
	@Override
	public String toString() {
		return "****";
	}
}
