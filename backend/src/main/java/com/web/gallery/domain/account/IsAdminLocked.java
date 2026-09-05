package com.web.gallery.domain.account;

import java.io.Serializable;

/**
 * 管理者ロックフラグの値オブジェクト
 *
 * <p>trueの場合、管理者が強制的にロックしたアカウントを表す。 ログイン失敗回数による自動ロック解除の対象外で、管理者による解除のみで解ける。
 *
 * @param value 管理者ロックフラグ
 */
public record IsAdminLocked(Boolean value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public IsAdminLocked {
    if (value == null) {
      throw new IllegalArgumentException("管理者ロックフラグはnullにできません");
    }
  }
}
