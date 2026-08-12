package com.web.gallery.domain.photo;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * 撮影日時の値オブジェクト
 *
 * @param	value	撮影日時
 */
public record PhotoAt(OffsetDateTime value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public PhotoAt {
		if (value == null) {
			throw new IllegalArgumentException("撮影日時はnullにできません");
		}
	}
}
