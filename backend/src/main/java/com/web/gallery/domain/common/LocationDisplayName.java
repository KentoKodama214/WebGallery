package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * ロケーション表示名の値オブジェクト
 *
 * <p>写真詳細等での表示に使用する名称。アカウント内での重複を許容する
 *
 * @param value ロケーション表示名
 */
public record LocationDisplayName(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public LocationDisplayName {
    if (value == null) {
      throw new IllegalArgumentException("ロケーション表示名はnullにできません");
    }
  }
}
