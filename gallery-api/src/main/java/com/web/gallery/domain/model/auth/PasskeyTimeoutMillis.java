package com.web.gallery.domain.model.auth;

import java.io.Serializable;

/**
 * パスキー操作のタイムアウト（ミリ秒）の値オブジェクト
 *
 * @param value パスキー操作のタイムアウト（ミリ秒）
 */
public record PasskeyTimeoutMillis(Long value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは0以下の場合
   */
  public PasskeyTimeoutMillis {
    if (value == null) {
      throw new IllegalArgumentException("パスキー操作のタイムアウト（ミリ秒）はnullにできません");
    }
    if (value <= 0) {
      throw new IllegalArgumentException("パスキー操作のタイムアウト（ミリ秒）は1以上にしてください");
    }
  }
}
