package com.web.gallery.domain.model.auth;

import java.io.Serializable;

/**
 * パスキー番号の値オブジェクト
 *
 * @param value パスキー番号
 */
public record PasskeyNo(Long value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public PasskeyNo {
    if (value == null) {
      throw new IllegalArgumentException("パスキー番号はnullにできません");
    }
  }
}
