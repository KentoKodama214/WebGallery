package com.web.gallery.domain.photo;

import java.io.Serializable;

/**
 * タグ英語名の値オブジェクト
 *
 * @param value タグ英語名
 */
public record TagEnglishName(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public TagEnglishName {
    if (value == null) {
      throw new IllegalArgumentException("タグ英語名はnullにできません");
    }
  }
}
