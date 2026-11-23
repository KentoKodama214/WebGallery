package com.web.gallery.domain.model.auth;

import java.io.Serializable;

/**
 * チャレンジの値オブジェクト
 *
 * @param value チャレンジ
 */
public record PasskeyChallenge(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは空白の場合
   */
  public PasskeyChallenge {
    if (value == null) {
      throw new IllegalArgumentException("チャレンジはnullにできません");
    }
    if (value.isBlank()) {
      throw new IllegalArgumentException("チャレンジは空白にできません");
    }
  }
}
