package com.web.gallery.domain.model.photo;

import java.io.Serializable;
import java.util.Map;

/**
 * 額縁定義の値オブジェクト
 *
 * <p>額縁のサイズ・背景・外枠・写真枠・メッセージ枠の配置とデザインを、JSONオブジェクトのままキーと値の組で保持する。 項目の妥当性は{@link
 * com.web.gallery.domain.service.FrameDefinitionPolicy}が判定する。入れ子のリスト・マップを含めた
 * 変更不可なコピーの作成とnull値の除去は{@link TemplateSettings}と同じ規則で行う
 *
 * @param value 設定項目名と値の組
 */
public record FrameDefinition(Map<String, Object> value) implements Serializable {

  /**
   * コンパクトコンストラクタ
   *
   * @throws IllegalArgumentException nullの場合
   */
  public FrameDefinition {
    if (value == null) {
      throw new IllegalArgumentException("額縁定義はnullにできません");
    }
    value = new TemplateSettings(value).value();
  }
}
