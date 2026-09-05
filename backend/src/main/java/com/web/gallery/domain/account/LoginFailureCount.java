package com.web.gallery.domain.account;

import java.io.Serializable;

/**
 * ログイン失敗回数の値オブジェクト
 *
 * @param value ログイン失敗回数
 */
public record LoginFailureCount(Integer value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは負の値の場合
   */
  public LoginFailureCount {
    if (value == null) {
      throw new IllegalArgumentException("ログイン失敗回数はnullにできません");
    }
    if (value < 0) {
      throw new IllegalArgumentException("ログイン失敗回数は0以上である必要があります");
    }
  }

  /**
   * 未設定の場合にDB保存用のデフォルト値を返す
   *
   * @param nullable {@link LoginFailureCount}（null許容）
   * @return nullableがnullでなければそのまま、nullであればデフォルト値を持つ{@link LoginFailureCount}
   */
  public static LoginFailureCount getOrDefault(LoginFailureCount nullable) {
    return nullable != null ? nullable : new LoginFailureCount(0);
  }
}
