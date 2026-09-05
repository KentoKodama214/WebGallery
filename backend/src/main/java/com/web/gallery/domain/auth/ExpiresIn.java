package com.web.gallery.domain.auth;

import java.io.Serializable;

/**
 * アクセストークン有効期限（秒）の値オブジェクト
 *
 * @param value 有効期限（秒）
 */
public record ExpiresIn(Long value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは負の値の場合
   */
  public ExpiresIn {
    if (value == null) {
      throw new IllegalArgumentException("有効期限はnullにできません");
    }
    if (value < 0) {
      throw new IllegalArgumentException("有効期限は0以上にしてください");
    }
  }
}
