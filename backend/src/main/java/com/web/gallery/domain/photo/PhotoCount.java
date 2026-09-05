package com.web.gallery.domain.photo;

import java.io.Serializable;

/**
 * 写真登録枚数の値オブジェクト
 *
 * @param value 写真登録枚数
 */
public record PhotoCount(Integer value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは負の値の場合
   */
  public PhotoCount {
    if (value == null) {
      throw new IllegalArgumentException("写真登録枚数はnullにできません");
    }
    if (value < 0) {
      throw new IllegalArgumentException("写真登録枚数は0以上である必要があります");
    }
  }
}
