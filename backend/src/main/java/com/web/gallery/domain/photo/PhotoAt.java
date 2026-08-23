package com.web.gallery.domain.photo;

import java.io.Serializable;
import java.time.OffsetDateTime;

import com.web.gallery.constant.Consts;

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

	/**
	 * 未設定の場合にDB保存用のデフォルト値を返す
	 *
	 * @param	nullable	{@link PhotoAt}（null許容）
	 * @return				nullableがnullでなければそのまま、nullであればデフォルト値を持つ{@link PhotoAt}
	 */
	public static PhotoAt getOrDefault(PhotoAt nullable) {
		return nullable != null ? nullable : new PhotoAt(Consts.MIN_OFFSET_DATE_TIME);
	}
}
