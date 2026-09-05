package com.web.gallery.domain.photo;

import java.io.Serializable;

/**
 * お気に入り数の値オブジェクト
 *
 * @param value お気に入り数
 */
public record FavoriteCount(Integer value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは負の値の場合
   */
  public FavoriteCount {
    if (value == null) {
      throw new IllegalArgumentException("お気に入り数はnullにできません");
    }
    if (value < 0) {
      throw new IllegalArgumentException("お気に入り数は0以上である必要があります");
    }
  }
}
