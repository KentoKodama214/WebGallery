package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * リファラ（遷移元URL）の値オブジェクト
 *
 * @param value リファラ（取得できない場合は空文字）
 */
public record Referer(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public Referer {
    if (value == null) {
      throw new IllegalArgumentException("リファラはnullにできません");
    }
  }
}
