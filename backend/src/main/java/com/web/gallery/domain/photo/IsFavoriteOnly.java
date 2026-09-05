package com.web.gallery.domain.photo;

import java.io.Serializable;

/**
 * お気に入りのみフィルタの値オブジェクト
 *
 * @param value お気に入りのみならtrue
 */
public record IsFavoriteOnly(Boolean value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public IsFavoriteOnly {
    if (value == null) {
      throw new IllegalArgumentException("お気に入りのみフィルタはnullにできません");
    }
  }
}
