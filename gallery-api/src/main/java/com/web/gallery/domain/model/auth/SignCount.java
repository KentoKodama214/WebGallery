package com.web.gallery.domain.model.auth;

import java.io.Serializable;

/**
 * 署名カウンタの値オブジェクト
 *
 * @param value 署名カウンタ
 */
public record SignCount(Long value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは負の値の場合
   */
  public SignCount {
    if (value == null) {
      throw new IllegalArgumentException("署名カウンタはnullにできません");
    }
    if (value < 0) {
      throw new IllegalArgumentException("署名カウンタは0以上にしてください");
    }
  }
}
