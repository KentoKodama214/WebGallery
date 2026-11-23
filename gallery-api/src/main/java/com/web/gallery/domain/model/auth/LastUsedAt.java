package com.web.gallery.domain.model.auth;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * 最終使用日時の値オブジェクト
 *
 * @param value 最終使用日時
 */
public record LastUsedAt(OffsetDateTime value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public LastUsedAt {
    if (value == null) {
      throw new IllegalArgumentException("最終使用日時はnullにできません");
    }
  }
}
