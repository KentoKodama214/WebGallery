package com.web.gallery.domain.inquiry;

import java.io.Serializable;

/**
 * お問い合わせ件名の値オブジェクト
 *
 * @param value お問い合わせ件名
 */
public record InquirySubject(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは空文字、100文字を超える場合
   */
  public InquirySubject {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("お問い合わせ件名はnullまたは空文字にできません");
    }
    if (value.length() > 100) {
      throw new IllegalArgumentException("お問い合わせ件名は100文字以内である必要があります");
    }
  }
}
