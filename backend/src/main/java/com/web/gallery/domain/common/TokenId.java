package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * トークンIDの値オブジェクト
 *
 * @param value トークンID
 */
public record TokenId(Long value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは0以下の場合
   */
  public TokenId {
    if (value == null) {
      throw new IllegalArgumentException("トークンIDはnullにできません");
    }
    if (value <= 0) {
      throw new IllegalArgumentException("トークンIDは正の値である必要があります");
    }
  }
}
