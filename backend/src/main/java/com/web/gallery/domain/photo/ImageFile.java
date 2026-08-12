package com.web.gallery.domain.photo;

import org.springframework.web.multipart.MultipartFile;

/**
 * 画像ファイルの値オブジェクト
 *
 * @param	value	画像ファイル
 */
public record ImageFile(MultipartFile value) {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullの場合
	 */
	public ImageFile {
		if (value == null) {
			throw new IllegalArgumentException("画像ファイルはnullにできません");
		}
	}
}
