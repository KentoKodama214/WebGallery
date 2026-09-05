package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * 更新者の値オブジェクト
 *
 * @param value 更新者
 */
public record UpdatedBy(Long value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public UpdatedBy {
    if (value == null) {
      throw new IllegalArgumentException("更新者はnullにできません");
    }
  }
}
