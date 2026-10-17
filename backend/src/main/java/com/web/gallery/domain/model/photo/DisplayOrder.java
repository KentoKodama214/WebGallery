package com.web.gallery.domain.model.photo;

import java.io.Serializable;

/**
 * ポートフォリオ内の写真の表示順の値オブジェクト
 *
 * @param value 表示順（1始まり）
 */
public record DisplayOrder(Integer value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは0以下の場合
   */
  public DisplayOrder {
    if (value == null) {
      throw new IllegalArgumentException("表示順はnullにできません");
    }
    if (value <= 0) {
      throw new IllegalArgumentException("表示順は正の値である必要があります");
    }
  }
}
