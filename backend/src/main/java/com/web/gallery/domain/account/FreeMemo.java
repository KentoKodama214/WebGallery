package com.web.gallery.domain.account;

import java.io.Serializable;

/**
 * フリーメモの値オブジェクト
 * <p>
 * null/空文字を許容する
 *
 * @param	value	フリーメモ
 */
public record FreeMemo(String value) implements Serializable {
	@Override
	public String toString() {
		return value == null ? "" : value;
	}
}
