package com.web.gallery.domain.photo;

import com.web.gallery.constant.Consts;
import java.io.Serializable;

/**
 * 写真タイトル日本語名の値オブジェクト
 *
 * @param value 写真タイトル日本語名
 */
public record PhotoJapaneseTitle(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは100文字を超える場合
   */
  public PhotoJapaneseTitle {
    if (value == null) {
      throw new IllegalArgumentException("写真タイトル日本語名はnullにできません");
    }
    if (value.length() > 100) {
      throw new IllegalArgumentException("写真タイトル日本語名は100文字以内である必要があります");
    }
  }

  /**
   * 未設定の場合にDB保存用のデフォルト値を返す
   *
   * @param nullable {@link PhotoJapaneseTitle}（null許容）
   * @return nullableがnullでなければそのまま、nullであればデフォルト値を持つ{@link PhotoJapaneseTitle}
   */
  public static PhotoJapaneseTitle getOrDefault(PhotoJapaneseTitle nullable) {
    return nullable != null ? nullable : new PhotoJapaneseTitle(Consts.STRING_EMPTY);
  }
}
