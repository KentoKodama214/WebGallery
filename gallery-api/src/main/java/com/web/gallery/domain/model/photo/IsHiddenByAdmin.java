package com.web.gallery.domain.model.photo;

import java.io.Serializable;

/**
 * 管理者非公開フラグの値オブジェクト
 *
 * @param value 管理者により非公開にされており、所有者本人以外には表示しないならtrue
 */
public record IsHiddenByAdmin(Boolean value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public IsHiddenByAdmin {
    if (value == null) {
      throw new IllegalArgumentException("管理者非公開フラグはnullにできません");
    }
  }
}
