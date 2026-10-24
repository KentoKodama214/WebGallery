package com.web.gallery.domain.model.photo;

import com.web.gallery.domain.constant.Consts;
import java.io.Serializable;

/**
 * ポートフォリオのテーマの値オブジェクト
 *
 * @param value テーマ
 */
public record PortfolioTheme(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは空文字、100文字を超える場合
   */
  public PortfolioTheme {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("ポートフォリオのテーマはnullまたは空文字にできません");
    }
    if (value.length() > Consts.PORTFOLIO_THEME_MAX_LENGTH) {
      throw new IllegalArgumentException("ポートフォリオのテーマは100文字以内である必要があります");
    }
  }
}
