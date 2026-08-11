package com.web.gallery.domain.common;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * 更新日時の値オブジェクト
 *
 * @param	value	更新日時
 */
public record UpdatedAt(OffsetDateTime value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public UpdatedAt {
		if (value == null) {
			throw new IllegalArgumentException("更新日時はnullにできません");
		}
	}
}
