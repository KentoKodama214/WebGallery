package com.web.gallery.domain.auth;

import java.io.Serializable;

/**
 * リフレッシュトークンの値オブジェクト
 *
 * @param value リフレッシュトークン
 */
public record RefreshTokenValue(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public RefreshTokenValue {
    if (value == null) {
      throw new IllegalArgumentException("リフレッシュトークンはnullにできません");
    }
  }
}
