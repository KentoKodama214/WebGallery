package com.web.gallery.infrastructure.persistence.entity.photo;

import com.web.gallery.application.model.photo.FrameTemplateModel;
import java.time.OffsetDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/** 額縁テンプレートマスタテーブルのEntityクラス */
@Data
@Builder
public class FrameTemplateMst {
  /** ID */
  private Long id;

  /** アカウント番号 */
  private Long accountNo;

  /** 額縁テンプレート番号 */
  private Long frameTemplateNo;

  /** 作成者 */
  private Long createdBy;

  /** 作成日時 */
  private OffsetDateTime createdAt;

  /** 更新者 */
  private Long updatedBy;

  /** 更新日時 */
  private OffsetDateTime updatedAt;

  /** テンプレート名 */
  private String templateName;

  /** 額縁定義（JSON） */
  private Map<String, Object> frameDefinition;

  /**
   * 新規登録用のFrameTemplateModelからFrameTemplateMstエンティティを生成する
   *
   * @param model 額縁テンプレート番号を採番済みの{@link FrameTemplateModel}
   * @return {@link FrameTemplateMst}
   */
  public static FrameTemplateMst fromForRegist(FrameTemplateModel model) {
    return FrameTemplateMst.builder()
        .accountNo(model.getAccountNo().value())
        .frameTemplateNo(model.getFrameTemplateNo().value())
        .createdBy(model.getAccountNo().value())
        .updatedBy(model.getAccountNo().value())
        .templateName(model.getTemplateName().value())
        .frameDefinition(model.getFrameDefinition().value())
        .build();
  }
}
