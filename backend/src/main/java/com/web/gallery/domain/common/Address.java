package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * 住所の値オブジェクト
 *
 * @param value 住所
 */
public record Address(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public Address {
    if (value == null) {
      throw new IllegalArgumentException("住所はnullにできません");
    }
  }
}
