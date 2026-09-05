package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * 区分グループ英語名の値オブジェクト
 *
 * @param value 区分グループ英語名
 */
public record KbnGroupEnglishName(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public KbnGroupEnglishName {
    if (value == null) {
      throw new IllegalArgumentException("区分グループ英語名はnullにできません");
    }
  }
}
