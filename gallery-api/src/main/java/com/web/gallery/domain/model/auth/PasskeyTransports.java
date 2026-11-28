package com.web.gallery.domain.model.auth;

import java.io.Serializable;

/**
 * 認証器の通信手段の値オブジェクト
 *
 * @param value 認証器の通信手段
 */
public record PasskeyTransports(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public PasskeyTransports {
    if (value == null) {
      throw new IllegalArgumentException("認証器の通信手段はnullにできません");
    }
  }
}
