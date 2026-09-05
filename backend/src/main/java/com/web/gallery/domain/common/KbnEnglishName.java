package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * 区分英語名の値オブジェクト
 *
 * @param value 区分英語名
 */
public record KbnEnglishName(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public KbnEnglishName {
    if (value == null) {
      throw new IllegalArgumentException("区分英語名はnullにできません");
    }
  }
}
