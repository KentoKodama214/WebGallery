package com.web.gallery.domain.account;

import java.io.Serializable;

/**
 * ログイン失敗回数の値オブジェクト
 *
 * @param	value	ログイン失敗回数
 */
public record LoginFailureCount(Integer value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullまたは負の値の場合
	 */
	public LoginFailureCount {
		if (value == null) {
			throw new IllegalArgumentException("ログイン失敗回数はnullにできません");
		}
		if (value < 0) {
			throw new IllegalArgumentException("ログイン失敗回数は0以上である必要があります");
		}
	}
}
