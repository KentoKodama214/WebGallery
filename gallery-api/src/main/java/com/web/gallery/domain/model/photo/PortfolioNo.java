package com.web.gallery.domain.model.photo;

import java.io.Serializable;
import java.util.Optional;

/**
 * ポートフォリオ番号の値オブジェクト
 *
 * @param value ポートフォリオ番号
 */
public record PortfolioNo(Long value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは0以下の場合
   */
  public PortfolioNo {
    if (value == null) {
      throw new IllegalArgumentException("ポートフォリオ番号はnullにできません");
    }
    if (value <= 0) {
      throw new IllegalArgumentException("ポートフォリオ番号は正の値である必要があります");
    }
  }

  /**
   * 現在登録されている最大ポートフォリオ番号から、新規採番するポートフォリオ番号を生成する
   *
   * @param maxPortfolioNo 現在登録されている最大ポートフォリオ番号（未登録の場合はnull）
   * @return 新規採番したポートフォリオ番号
   */
  public static PortfolioNo next(Long maxPortfolioNo) {
    return new PortfolioNo(Optional.ofNullable(maxPortfolioNo).map(num -> num + 1).orElse(1L));
  }
}
