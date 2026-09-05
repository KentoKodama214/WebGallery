package com.web.gallery.domain.photo;

import java.io.Serializable;

/**
 * ISOの値オブジェクト
 *
 * @param value ISO
 */
public record Iso(Integer value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは負の値の場合
   */
  public Iso {
    if (value == null) {
      throw new IllegalArgumentException("ISOはnullにできません");
    }
    if (value < 0) {
      throw new IllegalArgumentException("ISOは0以上である必要があります");
    }
  }

  /**
   * 未設定の場合にDB保存用のデフォルト値を返す
   *
   * @param nullable {@link Iso}（null許容）
   * @return nullableがnullでなければそのまま、nullであればデフォルト値を持つ{@link Iso}
   */
  public static Iso getOrDefault(Iso nullable) {
    return nullable != null ? nullable : new Iso(0);
  }
}
