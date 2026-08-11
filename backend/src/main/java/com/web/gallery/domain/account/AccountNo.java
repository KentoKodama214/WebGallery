package com.web.gallery.domain.account;

import java.io.Serializable;

/**
 * アカウント番号の値オブジェクト
 *
 * @param	value	アカウント番号
 */
public record AccountNo(Long value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullまたは0以下の場合
	 */
	public AccountNo {
		if (value == null) {
			throw new IllegalArgumentException("アカウント番号はnullにできません");
		}
		if (value <= 0) {
			throw new IllegalArgumentException("アカウント番号は正の値である必要があります");
		}
	}
}
