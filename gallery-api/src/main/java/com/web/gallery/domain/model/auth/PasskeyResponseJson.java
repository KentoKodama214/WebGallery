package com.web.gallery.domain.model.auth;

import java.io.Serializable;

/**
 * パスキーのレスポンスJSONの値オブジェクト
 *
 * @param value パスキーのレスポンスJSON
 */
public record PasskeyResponseJson(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは空白の場合
   */
  public PasskeyResponseJson {
    if (value == null) {
      throw new IllegalArgumentException("パスキーのレスポンスJSONはnullにできません");
    }
    if (value.isBlank()) {
      throw new IllegalArgumentException("パスキーのレスポンスJSONは空白にできません");
    }
  }
}
