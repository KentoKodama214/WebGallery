package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * IPアドレスの値オブジェクト
 *
 * @param value IPアドレス
 */
public record IpAddress(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public IpAddress {
    if (value == null) {
      throw new IllegalArgumentException("IPアドレスはnullにできません");
    }
  }
}
