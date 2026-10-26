package com.web.gallery.domain.model.photo;

import java.io.Serializable;

/**
 * アップロード可能な画像ファイルサイズ上限（MB単位）の値オブジェクト
 *
 * @param value ファイルサイズ上限（MB）
 */
public record MaxFileSizeMb(Integer value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは0以下の値の場合
   */
  public MaxFileSizeMb {
    if (value == null) {
      throw new IllegalArgumentException("ファイルサイズ上限はnullにできません");
    }
    if (value <= 0) {
      throw new IllegalArgumentException("ファイルサイズ上限は1以上である必要があります");
    }
  }
}
