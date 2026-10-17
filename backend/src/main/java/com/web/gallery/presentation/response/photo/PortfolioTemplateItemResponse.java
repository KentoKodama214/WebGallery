package com.web.gallery.presentation.response.photo;

import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/** ポートフォリオのテンプレート1件分のレスポンスパラメータを保持するクラス */
@Schema(description = "ポートフォリオのテンプレート")
@Data
@Builder
public class PortfolioTemplateItemResponse {
  /**
   * テンプレート区分
   *
   * <p>{@link PortfolioTemplateEnum}
   */
  @Schema(description = "テンプレート区分")
  private PortfolioTemplateEnum templateKbn;

  /** 表示名 */
  @Schema(description = "表示名", example = "フィーチャー")
  private String displayName;

  /**
   * PortfolioTemplateEnumからPortfolioTemplateItemResponseを生成する
   *
   * @param templateKbn {@link PortfolioTemplateEnum}
   * @return {@link PortfolioTemplateItemResponse}
   */
  public static PortfolioTemplateItemResponse of(PortfolioTemplateEnum templateKbn) {
    return PortfolioTemplateItemResponse.builder()
        .templateKbn(templateKbn)
        .displayName(templateKbn.getDisplayName())
        .build();
  }
}
