package com.web.gallery.domain.common;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * 有効期限の値オブジェクト
 *
 * @param	value	有効期限
 */
public record ExpiresAt(OffsetDateTime value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public ExpiresAt {
		if (value == null) {
			throw new IllegalArgumentException("有効期限はnullにできません");
		}
	}
}
