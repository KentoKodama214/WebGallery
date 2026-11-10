package com.web.gallery.domain.model.photo;

import com.web.gallery.domain.constant.Consts;
import java.io.Serializable;

/**
 * 額縁テンプレートのテンプレート名の値オブジェクト
 *
 * @param value テンプレート名
 */
public record FrameTemplateName(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは空文字、50文字を超える場合
   */
  public FrameTemplateName {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("額縁テンプレートのテンプレート名はnullまたは空文字にできません");
    }
    if (value.length() > Consts.FRAME_TEMPLATE_NAME_MAX_LENGTH) {
      throw new IllegalArgumentException("額縁テンプレートのテンプレート名は50文字以内である必要があります");
    }
  }
}
