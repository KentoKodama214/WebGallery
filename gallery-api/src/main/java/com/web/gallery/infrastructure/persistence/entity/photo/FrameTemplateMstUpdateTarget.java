package com.web.gallery.infrastructure.persistence.entity.photo;

import com.web.gallery.application.model.photo.FrameTemplateModel;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/** 額縁テンプレートマスタテーブルの更新対象クラス */
@Data
@Builder
public class FrameTemplateMstUpdateTarget {
  /** 更新者 */
  private Long updatedBy;

  /** テンプレート名 */
  private String templateName;

  /** 額縁定義（JSON） */
  private Map<String, Object> frameDefinition;

  /**
   * 内容の更新に伴う更新対象を生成する
   *
   * @param model 更新後の{@link FrameTemplateModel}
   * @return {@link FrameTemplateMstUpdateTarget}
   */
  public static FrameTemplateMstUpdateTarget forUpdate(FrameTemplateModel model) {
    return FrameTemplateMstUpdateTarget.builder()
        .updatedBy(model.getAccountNo().value())
        .templateName(model.getTemplateName().value())
        .frameDefinition(model.getFrameDefinition().value())
        .build();
  }
}
