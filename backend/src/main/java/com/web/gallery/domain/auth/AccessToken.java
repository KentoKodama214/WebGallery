package com.web.gallery.domain.auth;

import java.io.Serializable;

/**
 * アクセストークンの値オブジェクト
 *
 * @param	value	アクセストークン
 */
public record AccessToken(String value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public AccessToken {
		if (value == null) {
			throw new IllegalArgumentException("アクセストークンはnullにできません");
		}
	}
}
