package com.web.gallery.domain.photo;

import java.io.Serializable;

/**
 * タグ番号の値オブジェクト
 *
 * @param value タグ番号
 */
public record TagNo(Long value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは0以下の場合
   */
  public TagNo {
    if (value == null) {
      throw new IllegalArgumentException("タグ番号はnullにできません");
    }
    if (value <= 0) {
      throw new IllegalArgumentException("タグ番号は正の値である必要があります");
    }
  }
}
