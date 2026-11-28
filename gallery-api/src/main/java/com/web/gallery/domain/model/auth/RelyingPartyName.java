package com.web.gallery.domain.model.auth;

import java.io.Serializable;

/**
 * RP名の値オブジェクト
 *
 * @param value RP名
 */
public record RelyingPartyName(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは空白の場合
   */
  public RelyingPartyName {
    if (value == null) {
      throw new IllegalArgumentException("RP名はnullにできません");
    }
    if (value.isBlank()) {
      throw new IllegalArgumentException("RP名は空白にできません");
    }
  }
}
