package com.web.gallery.domain.model.auth;

import java.io.Serializable;

/**
 * ユーザーハンドルの値オブジェクト
 *
 * @param value ユーザーハンドル
 */
public record PasskeyUserHandle(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは空白の場合
   */
  public PasskeyUserHandle {
    if (value == null) {
      throw new IllegalArgumentException("ユーザーハンドルはnullにできません");
    }
    if (value.isBlank()) {
      throw new IllegalArgumentException("ユーザーハンドルは空白にできません");
    }
  }
}
