package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * 作成者の値オブジェクト
 *
 * @param value 作成者
 */
public record CreatedBy(Long value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public CreatedBy {
    if (value == null) {
      throw new IllegalArgumentException("作成者はnullにできません");
    }
  }
}
