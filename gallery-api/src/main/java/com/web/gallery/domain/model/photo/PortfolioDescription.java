package com.web.gallery.domain.model.photo;

import com.web.gallery.domain.constant.Consts;
import java.io.Serializable;

/**
 * ポートフォリオの説明文の値オブジェクト
 *
 * @param value 説明文
 */
public record PortfolioDescription(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは2000文字を超える場合
   */
  public PortfolioDescription {
    if (value == null) {
      throw new IllegalArgumentException("ポートフォリオの説明文はnullにできません");
    }
    if (value.length() > Consts.PORTFOLIO_DESCRIPTION_MAX_LENGTH) {
      throw new IllegalArgumentException("ポートフォリオの説明文は2000文字以内である必要があります");
    }
  }

  /**
   * 未設定の場合にDB保存用のデフォルト値を返す
   *
   * @param nullable {@link PortfolioDescription}（null許容）
   * @return nullableがnullでなければそのまま、nullであればデフォルト値を持つ{@link PortfolioDescription}
   */
  public static PortfolioDescription getOrDefault(PortfolioDescription nullable) {
    return nullable != null ? nullable : new PortfolioDescription(Consts.STRING_EMPTY);
  }
}
