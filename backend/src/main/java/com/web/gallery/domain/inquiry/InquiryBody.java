package com.web.gallery.domain.inquiry;

import java.io.Serializable;

/**
 * お問い合わせ本文の値オブジェクト
 *
 * @param value お問い合わせ本文
 */
public record InquiryBody(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは空文字、2000文字を超える場合
   */
  public InquiryBody {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("お問い合わせ本文はnullまたは空文字にできません");
    }
    if (value.length() > 2000) {
      throw new IllegalArgumentException("お問い合わせ本文は2000文字以内である必要があります");
    }
  }
}
