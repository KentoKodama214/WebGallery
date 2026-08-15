package com.web.gallery.domain.photo;

import java.io.Serializable;

/**
 * 画像ファイルパスの値オブジェクト
 *
 * @param	value	画像ファイルパス
 */
public record ImageFilePath(String value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public ImageFilePath {
		if (value == null) {
			throw new IllegalArgumentException("画像ファイルパスはnullにできません");
		}
	}
}
