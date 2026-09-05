package com.web.gallery.domain.photo;

import java.io.Serializable;

/**
 * タグ日本語名の値オブジェクト
 *
 * @param value タグ日本語名
 */
public record TagJapaneseName(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public TagJapaneseName {
    if (value == null) {
      throw new IllegalArgumentException("タグ日本語名はnullにできません");
    }
  }
}
