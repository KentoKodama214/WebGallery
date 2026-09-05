package com.web.gallery.domain.common;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 緯度の値オブジェクト
 *
 * @param value 緯度
 */
public record Latitude(BigDecimal value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public Latitude {
    if (value == null) {
      throw new IllegalArgumentException("緯度はnullにできません");
    }
  }
}
