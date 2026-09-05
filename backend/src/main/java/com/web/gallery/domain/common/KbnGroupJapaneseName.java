package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * 区分グループ日本語名の値オブジェクト
 *
 * @param value 区分グループ日本語名
 */
public record KbnGroupJapaneseName(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public KbnGroupJapaneseName {
    if (value == null) {
      throw new IllegalArgumentException("区分グループ日本語名はnullにできません");
    }
  }
}
