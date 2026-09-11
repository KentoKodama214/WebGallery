package com.web.gallery.domain.photo;

import java.io.Serializable;

/**
 * 位置情報公開フラグの値オブジェクト
 *
 * @param value 撮影場所（緯度経度・住所・ロケーション名）を本人以外にも公開するならtrue
 */
public record IsLocationPublic(Boolean value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public IsLocationPublic {
    if (value == null) {
      throw new IllegalArgumentException("位置情報公開フラグはnullにできません");
    }
  }
}
