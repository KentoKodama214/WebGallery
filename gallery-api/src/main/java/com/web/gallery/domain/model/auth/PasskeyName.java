package com.web.gallery.domain.model.auth;

import java.io.Serializable;

/**
 * パスキー名の値オブジェクト
 *
 * @param value パスキー名
 */
public record PasskeyName(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは空白、または50文字を超える場合
   */
  public PasskeyName {
    if (value == null) {
      throw new IllegalArgumentException("パスキー名はnullにできません");
    }
    if (value.isBlank()) {
      throw new IllegalArgumentException("パスキー名は空白にできません");
    }
    if (value.length() > 50) {
      throw new IllegalArgumentException("パスキー名は50文字以内にしてください");
    }
  }
}
