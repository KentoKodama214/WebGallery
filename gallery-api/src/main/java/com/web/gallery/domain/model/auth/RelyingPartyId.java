package com.web.gallery.domain.model.auth;

import java.io.Serializable;

/**
 * RP IDの値オブジェクト
 *
 * @param value RP ID
 */
public record RelyingPartyId(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは空白の場合
   */
  public RelyingPartyId {
    if (value == null) {
      throw new IllegalArgumentException("RP IDはnullにできません");
    }
    if (value.isBlank()) {
      throw new IllegalArgumentException("RP IDは空白にできません");
    }
  }
}
