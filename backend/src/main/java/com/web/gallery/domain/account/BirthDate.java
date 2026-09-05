package com.web.gallery.domain.account;

import com.web.gallery.constant.Consts;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 生年月日の値オブジェクト
 *
 * @param value 生年月日
 */
public record BirthDate(LocalDate value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public BirthDate {
    if (value == null) {
      throw new IllegalArgumentException("生年月日はnullにできません");
    }
  }

  /**
   * 未設定の場合にDB保存用のデフォルト値を返す
   *
   * @param nullable {@link BirthDate}（null許容）
   * @return nullableがnullでなければそのまま、nullであればデフォルト値を持つ{@link BirthDate}
   */
  public static BirthDate getOrDefault(BirthDate nullable) {
    return nullable != null ? nullable : new BirthDate(Consts.MIN_LOCAL_DATE);
  }
}
