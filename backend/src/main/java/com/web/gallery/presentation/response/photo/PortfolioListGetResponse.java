package com.web.gallery.presentation.response.photo;

import com.web.gallery.application.model.photo.PortfolioModelList;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** 本人のポートフォリオ一覧のレスポンスパラメータを保持するクラス */
@Schema(description = "ポートフォリオ一覧レスポンス")
@Data
@Builder
public class PortfolioListGetResponse {
  /** ポートフォリオ一覧 */
  @Schema(description = "ポートフォリオ一覧（表示順）")
  private List<PortfolioListItemResponse> portfolioList;

  /**
   * PortfolioModelListからPortfolioListGetResponseを生成する
   *
   * @param modelList {@link PortfolioModelList}
   * @return {@link PortfolioListGetResponse}
   */
  public static PortfolioListGetResponse from(PortfolioModelList modelList) {
    return PortfolioListGetResponse.builder()
        .portfolioList(modelList.stream().map(PortfolioListItemResponse::from).toList())
        .build();
  }
}
