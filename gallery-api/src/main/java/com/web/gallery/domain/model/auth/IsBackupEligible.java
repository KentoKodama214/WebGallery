package com.web.gallery.domain.model.auth;

import java.io.Serializable;

/**
 * バックアップ可能フラグの値オブジェクト
 *
 * @param value バックアップ可能フラグ
 */
public record IsBackupEligible(Boolean value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public IsBackupEligible {
    if (value == null) {
      throw new IllegalArgumentException("バックアップ可能フラグはnullにできません");
    }
  }
}
