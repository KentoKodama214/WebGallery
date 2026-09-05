package com.web.gallery.domain.common;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 経度の値オブジェクト
 *
 * @param value 経度
 */
public record Longitude(BigDecimal value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public Longitude {
    if (value == null) {
      throw new IllegalArgumentException("経度はnullにできません");
    }
  }
}
