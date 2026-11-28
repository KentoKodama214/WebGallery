package com.web.gallery.presentation.response.photo;

import com.web.gallery.domain.enumeration.GridRowPatternEnum;
import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Arrays;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** ポートフォリオのテンプレート一覧のレスポンスパラメータを保持するクラス */
@Schema(description = "ポートフォリオテンプレート一覧レスポンス")
@Data
@Builder
public class PortfolioTemplateListGetResponse {
  /** テンプレート一覧 */
  @Schema(description = "テンプレート一覧")
  private List<PortfolioTemplateItemResponse> templateList;

  /** グリッド型テンプレートの行パターン一覧 */
  @Schema(description = "グリッド型テンプレートの行パターン一覧")
  private List<GridRowPatternItemResponse> gridRowPatternList;

  /**
   * 定義済みのテンプレート・行パターンからPortfolioTemplateListGetResponseを生成する
   *
   * @return {@link PortfolioTemplateListGetResponse}
   */
  public static PortfolioTemplateListGetResponse of() {
    return PortfolioTemplateListGetResponse.builder()
        .templateList(
            Arrays.stream(PortfolioTemplateEnum.values())
                .map(PortfolioTemplateItemResponse::of)
                .toList())
        .gridRowPatternList(
            Arrays.stream(GridRowPatternEnum.values()).map(GridRowPatternItemResponse::of).toList())
        .build();
  }
}
