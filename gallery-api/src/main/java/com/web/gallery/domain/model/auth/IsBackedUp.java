package com.web.gallery.domain.model.auth;

import java.io.Serializable;

/**
 * バックアップ済みフラグの値オブジェクト
 *
 * @param value バックアップ済みフラグ
 */
public record IsBackedUp(Boolean value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public IsBackedUp {
    if (value == null) {
      throw new IllegalArgumentException("バックアップ済みフラグはnullにできません");
    }
  }
}
