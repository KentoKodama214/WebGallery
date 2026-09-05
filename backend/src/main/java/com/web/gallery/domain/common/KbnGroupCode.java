package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * 区分グループコードの値オブジェクト
 *
 * @param value 区分グループコード
 */
public record KbnGroupCode(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public KbnGroupCode {
    if (value == null) {
      throw new IllegalArgumentException("区分グループコードはnullにできません");
    }
  }
}
