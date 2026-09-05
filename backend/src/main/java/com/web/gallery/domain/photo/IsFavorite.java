package com.web.gallery.domain.photo;

import java.io.Serializable;

/**
 * お気に入りの値オブジェクト
 *
 * @param value お気に入りならtrue
 */
public record IsFavorite(Boolean value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public IsFavorite {
    if (value == null) {
      throw new IllegalArgumentException("お気に入りはnullにできません");
    }
  }
}
