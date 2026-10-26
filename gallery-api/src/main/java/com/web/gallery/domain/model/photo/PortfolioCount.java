package com.web.gallery.domain.model.photo;

import java.io.Serializable;

/**
 * ポートフォリオ作成数の値オブジェクト
 *
 * @param value ポートフォリオ作成数
 */
public record PortfolioCount(Integer value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは負の値の場合
   */
  public PortfolioCount {
    if (value == null) {
      throw new IllegalArgumentException("ポートフォリオ作成数はnullにできません");
    }
    if (value < 0) {
      throw new IllegalArgumentException("ポートフォリオ作成数は0以上である必要があります");
    }
  }
}
