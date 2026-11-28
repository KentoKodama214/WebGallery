package com.web.gallery.domain.model.photo;

import java.io.Serializable;
import java.util.Optional;

/**
 * 額縁テンプレート番号の値オブジェクト
 *
 * @param value 額縁テンプレート番号
 */
public record FrameTemplateNo(Long value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは0以下の場合
   */
  public FrameTemplateNo {
    if (value == null) {
      throw new IllegalArgumentException("額縁テンプレート番号はnullにできません");
    }
    if (value <= 0) {
      throw new IllegalArgumentException("額縁テンプレート番号は正の値である必要があります");
    }
  }

  /**
   * 現在登録されている最大額縁テンプレート番号から、新規採番する額縁テンプレート番号を生成する
   *
   * @param maxFrameTemplateNo 現在登録されている最大額縁テンプレート番号（未登録の場合はnull）
   * @return 新規採番した額縁テンプレート番号
   */
  public static FrameTemplateNo next(Long maxFrameTemplateNo) {
    return new FrameTemplateNo(
        Optional.ofNullable(maxFrameTemplateNo).map(num -> num + 1).orElse(1L));
  }
}
