package com.web.gallery.domain.inquiry;

import java.io.Serializable;
import java.util.Optional;

/**
 * お問い合わせ返信番号の値オブジェクト
 *
 * @param value 返信番号
 */
public record ReplyNo(Long value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは0以下の場合
   */
  public ReplyNo {
    if (value == null) {
      throw new IllegalArgumentException("返信番号はnullにできません");
    }
    if (value <= 0) {
      throw new IllegalArgumentException("返信番号は正の値である必要があります");
    }
  }

  /**
   * 現在登録されている最大返信番号から、新規採番する返信番号を生成する
   *
   * @param maxReplyNo 現在登録されている最大返信番号（未登録の場合はnull）
   * @return 新規採番した返信番号
   */
  public static ReplyNo next(Long maxReplyNo) {
    return new ReplyNo(Optional.ofNullable(maxReplyNo).map(num -> num + 1).orElse(1L));
  }
}
