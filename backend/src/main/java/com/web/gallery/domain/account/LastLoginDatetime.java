package com.web.gallery.domain.account;

import com.web.gallery.constant.Consts;
import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * 最終ログイン日時の値オブジェクト
 *
 * @param value 最終ログイン日時
 */
public record LastLoginDatetime(OffsetDateTime value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public LastLoginDatetime {
    if (value == null) {
      throw new IllegalArgumentException("最終ログイン日時はnullにできません");
    }
  }

  /**
   * 未設定の場合にDB保存用のデフォルト値を返す
   *
   * @param nullable {@link LastLoginDatetime}（null許容）
   * @return nullableがnullでなければそのまま、nullであればデフォルト値を持つ{@link LastLoginDatetime}
   */
  public static LastLoginDatetime getOrDefault(LastLoginDatetime nullable) {
    return nullable != null ? nullable : new LastLoginDatetime(Consts.MIN_OFFSET_DATE_TIME);
  }
}
