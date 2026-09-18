package com.web.gallery.domain.inquiry;

import java.io.Serializable;

/**
 * お問い合わせ返信本文の値オブジェクト
 *
 * @param value 返信本文
 */
public record ReplyBody(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは空文字、2000文字を超える場合
   */
  public ReplyBody {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("返信本文はnullまたは空文字にできません");
    }
    if (value.length() > 2000) {
      throw new IllegalArgumentException("返信本文は2000文字以内である必要があります");
    }
  }
}
