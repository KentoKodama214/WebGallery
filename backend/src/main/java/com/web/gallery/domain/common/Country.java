package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * 国（ISO 3166-1 alpha-2コード）の値オブジェクト
 *
 * @param value 国コード
 */
public record Country(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public Country {
    if (value == null) {
      throw new IllegalArgumentException("国コードはnullにできません");
    }
  }
}
