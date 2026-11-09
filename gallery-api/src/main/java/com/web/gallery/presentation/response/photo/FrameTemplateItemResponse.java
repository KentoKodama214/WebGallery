package com.web.gallery.presentation.response.photo;

import com.web.gallery.application.model.photo.FrameTemplateModel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/** 額縁テンプレート1件分（一覧の1件・詳細）のレスポンスパラメータを保持するクラス */
@Schema(description = "額縁テンプレート")
@Data
@Builder
public class FrameTemplateItemResponse {
  /** 額縁テンプレート番号 */
  @Schema(description = "額縁テンプレート番号", example = "1")
  private Long frameTemplateNo;

  /** テンプレート名 */
  @Schema(description = "テンプレート名", example = "四つ切 3枚組")
  private String templateName;

  /** 額縁定義 */
  @Schema(description = "額縁定義")
  private Map<String, Object> frameDefinition;

  /** 作成日時 */
  @Schema(description = "作成日時")
  private OffsetDateTime createdAt;

  /** 更新日時 */
  @Schema(description = "更新日時")
  private OffsetDateTime updatedAt;

  /**
   * FrameTemplateModelからFrameTemplateItemResponseを生成する
   *
   * @param model {@link FrameTemplateModel}
   * @return {@link FrameTemplateItemResponse}
   */
  public static FrameTemplateItemResponse from(FrameTemplateModel model) {
    return FrameTemplateItemResponse.builder()
        .frameTemplateNo(model.getFrameTemplateNo().value())
        .templateName(model.getTemplateName().value())
        .frameDefinition(model.getFrameDefinition().value())
        .createdAt(model.getCreatedAt())
        .updatedAt(model.getUpdatedAt())
        .build();
  }
}
