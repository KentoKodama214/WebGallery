package com.web.gallery.domain.photo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * シャッタースピードの値オブジェクト
 *
 * @param	value	シャッタースピード
 */
public record ShutterSpeed(BigDecimal value) implements Serializable {

	/**
	 * コンパクトコンストラクタ
	 *
	 * @throws	IllegalArgumentException	nullまたは負の値の場合
	 */
	public ShutterSpeed {
		if (value == null) {
			throw new IllegalArgumentException("シャッタースピードはnullにできません");
		}
		if (value.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("シャッタースピードは0以上である必要があります");
		}
	}

	/**
	 * 未設定の場合にDB保存用のデフォルト値を返す
	 *
	 * @param	nullable	{@link ShutterSpeed}（null許容）
	 * @return				nullableがnullでなければそのまま、nullであればデフォルト値を持つ{@link ShutterSpeed}
	 */
	public static ShutterSpeed getOrDefault(ShutterSpeed nullable) {
		return nullable != null ? nullable : new ShutterSpeed(BigDecimal.ZERO);
	}
}
