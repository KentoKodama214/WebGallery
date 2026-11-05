package com.web.gallery.domain.model.photo;

import com.web.gallery.domain.constant.Consts;
import java.io.Serializable;

/**
 * 管理者が写真を非公開・非公開解除した理由の値オブジェクト
 *
 * @param value 理由
 */
public record ModerationReason(String value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullまたは空文字、500文字を超える場合
   */
  public ModerationReason {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("理由はnullまたは空文字にできません");
    }
    if (value.length() > Consts.MODERATION_REASON_MAX_LENGTH) {
      throw new IllegalArgumentException("理由は500文字以内である必要があります");
    }
  }
}
