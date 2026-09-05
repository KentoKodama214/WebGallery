package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * トークンハッシュの値オブジェクト
 *
 * @param value トークンハッシュ
 */
public record TokenHash(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public TokenHash {
    if (value == null) {
      throw new IllegalArgumentException("トークンハッシュはnullにできません");
    }
  }
}
