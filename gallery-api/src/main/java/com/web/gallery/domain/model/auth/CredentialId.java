package com.web.gallery.domain.model.auth;

import java.io.Serializable;

/**
 * クレデンシャルIDの値オブジェクト
 *
 * @param value クレデンシャルID
 */
public record CredentialId(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは空白の場合
   */
  public CredentialId {
    if (value == null) {
      throw new IllegalArgumentException("クレデンシャルIDはnullにできません");
    }
    if (value.isBlank()) {
      throw new IllegalArgumentException("クレデンシャルIDは空白にできません");
    }
  }
}
