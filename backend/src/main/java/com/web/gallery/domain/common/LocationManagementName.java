package com.web.gallery.domain.common;

import java.io.Serializable;

/**
 * ロケーション管理名の値オブジェクト
 *
 * <p>アカウント内で一意な、ロケーションマスタの管理・検索用の名称（既存マスタからの選択・重複判定に使用）
 *
 * @param value ロケーション管理名
 */
public record LocationManagementName(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public LocationManagementName {
    if (value == null) {
      throw new IllegalArgumentException("ロケーション管理名はnullにできません");
    }
  }
}
