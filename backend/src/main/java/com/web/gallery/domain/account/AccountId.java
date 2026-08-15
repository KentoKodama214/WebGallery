package com.web.gallery.domain.account;

import java.io.Serializable;

/**
 * アカウントIDの値オブジェクト
 *
 * @param	value	アカウントID
 */
public record AccountId(String value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullまたはパターン不一致の場合
	 */
	public AccountId {
		if (value == null) {
			throw new IllegalArgumentException("アカウントIDはnullにできません");
		}
		if (!value.matches("^[a-zA-Z0-9]{8,16}$")) {
			throw new IllegalArgumentException("アカウントIDは半角英数字8〜16文字である必要があります");
		}
	}
}
