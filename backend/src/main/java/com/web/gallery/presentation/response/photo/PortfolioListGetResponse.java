package com.web.gallery.presentation.response.photo;

import com.web.gallery.application.model.photo.PortfolioPageModel;
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

  /** 最後のページかどうか */
  @Schema(description = "最後のページかどうか")
  private Boolean isLast;

  /**
   * PortfolioPageModelからPortfolioListGetResponseを生成する
   *
   * @param pageModel {@link PortfolioPageModel}
   * @return {@link PortfolioListGetResponse}
   */
  public static PortfolioListGetResponse from(PortfolioPageModel pageModel) {
    return PortfolioListGetResponse.builder()
        .portfolioList(
            pageModel.getPortfolioModelList().stream()
                .map(PortfolioListItemResponse::from)
                .toList())
        .isLast(pageModel.getIsLast())
        .build();
  }
}
