package com.web.gallery.domain.common;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * 作成日時の値オブジェクト
 *
 * @param	value	作成日時
 */
public record CreatedAt(OffsetDateTime value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public CreatedAt {
		if (value == null) {
			throw new IllegalArgumentException("作成日時はnullにできません");
		}
	}
}
