package com.web.gallery.domain.photo;

import java.io.Serializable;
import java.util.Optional;

/**
 * 写真番号の値オブジェクト
 *
 * @param	value	写真番号
 */
public record PhotoNo(Long value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullまたは0以下の場合
	 */
	public PhotoNo {
		if (value == null) {
			throw new IllegalArgumentException("写真番号はnullにできません");
		}
		if (value <= 0) {
			throw new IllegalArgumentException("写真番号は正の値である必要があります");
		}
	}

	/**
	 * 現在登録されている最大写真番号から、新規採番する写真番号を生成する
	 *
	 * @param	maxPhotoNo	現在登録されている最大写真番号（未登録の場合はnull）
	 * @return				新規採番した写真番号
	 */
	public static PhotoNo next(Long maxPhotoNo) {
		return new PhotoNo(Optional.ofNullable(maxPhotoNo).map(num -> num + 1).orElse(1L));
	}
}
