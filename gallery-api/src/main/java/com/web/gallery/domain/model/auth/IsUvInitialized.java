package com.web.gallery.domain.model.auth;

import java.io.Serializable;

/**
 * ユーザー検証済みフラグの値オブジェクト
 *
 * @param value ユーザー検証済みフラグ
 */
public record IsUvInitialized(Boolean value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public IsUvInitialized {
    if (value == null) {
      throw new IllegalArgumentException("ユーザー検証済みフラグはnullにできません");
    }
  }
}
