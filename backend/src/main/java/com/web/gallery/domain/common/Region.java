package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * 地域（都道府県・州等のサブディビジョン名）の値オブジェクト
 *
 * @param value 地域名
 */
public record Region(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public Region {
    if (value == null) {
      throw new IllegalArgumentException("地域名はnullにできません");
    }
  }
}
