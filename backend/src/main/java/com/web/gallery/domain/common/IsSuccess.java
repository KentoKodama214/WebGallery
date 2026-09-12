package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * 成功フラグの値オブジェクト
 *
 * @param value 成功フラグ
 */
public record IsSuccess(Boolean value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public IsSuccess {
    if (value == null) {
      throw new IllegalArgumentException("成功フラグはnullにできません");
    }
  }
}
