package com.web.gallery.domain.account;

import java.io.Serializable;

/**
 * アカウント名の値オブジェクト
 *
 * @param value アカウント名
 */
public record AccountName(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは空白の場合
   */
  public AccountName {
    if (value == null) {
      throw new IllegalArgumentException("アカウント名はnullにできません");
    }
    if (value.isBlank()) {
      throw new IllegalArgumentException("アカウント名は空白にできません");
    }
  }
}
