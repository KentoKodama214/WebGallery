package com.web.gallery.domain.inquiry;

import java.io.Serializable;
import java.util.Optional;

/**
 * お問い合わせ番号の値オブジェクト
 *
 * @param value お問い合わせ番号
 */
public record InquiryNo(Long value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは0以下の場合
   */
  public InquiryNo {
    if (value == null) {
      throw new IllegalArgumentException("お問い合わせ番号はnullにできません");
    }
    if (value <= 0) {
      throw new IllegalArgumentException("お問い合わせ番号は正の値である必要があります");
    }
  }

  /**
   * 現在登録されている最大お問い合わせ番号から、新規採番するお問い合わせ番号を生成する
   *
   * @param maxInquiryNo 現在登録されている最大お問い合わせ番号（未登録の場合はnull）
   * @return 新規採番したお問い合わせ番号
   */
  public static InquiryNo next(Long maxInquiryNo) {
    return new InquiryNo(Optional.ofNullable(maxInquiryNo).map(num -> num + 1).orElse(1L));
  }
}
