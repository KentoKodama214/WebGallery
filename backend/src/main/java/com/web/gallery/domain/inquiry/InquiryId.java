package com.web.gallery.domain.inquiry;

import java.io.Serializable;

/**
 * お問い合わせIDの値オブジェクト
 *
 * <p>お問い合わせマスタのサロゲートキー。管理者APIのパスパラメータ・返信の外部キーとして使用する
 *
 * @param value お問い合わせID
 */
public record InquiryId(Long value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは0以下の場合
   */
  public InquiryId {
    if (value == null) {
      throw new IllegalArgumentException("お問い合わせIDはnullにできません");
    }
    if (value <= 0) {
      throw new IllegalArgumentException("お問い合わせIDは正の値である必要があります");
    }
  }
}
