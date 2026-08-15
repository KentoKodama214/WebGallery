package com.web.gallery.domain.account;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * 最終ログイン日時の値オブジェクト
 *
 * @param	value	最終ログイン日時
 */
public record LastLoginDatetime(OffsetDateTime value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public LastLoginDatetime {
		if (value == null) {
			throw new IllegalArgumentException("最終ログイン日時はnullにできません");
		}
	}
}
