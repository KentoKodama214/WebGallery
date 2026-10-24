package com.web.gallery.presentation.response.photo;

import com.web.gallery.application.model.photo.PortfolioPhotoPageModel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** ポートフォリオ用の写真検索のレスポンスパラメータを保持するクラス */
@Schema(description = "ポートフォリオ用の写真検索レスポンス")
@Data
@Builder
public class PortfolioPhotoSearchGetResponse {
  /** 写真一覧 */
  @Schema(description = "写真一覧（撮影日時の新しい順）")
  private List<PortfolioPhotoSearchItemResponse> photoList;

  /** 最後のページかどうか */
  @Schema(description = "最後のページかどうか")
  private Boolean isLast;

  /**
   * PortfolioPhotoPageModelからPortfolioPhotoSearchGetResponseを生成する
   *
   * @param pageModel {@link PortfolioPhotoPageModel}
   * @return {@link PortfolioPhotoSearchGetResponse}
   */
  public static PortfolioPhotoSearchGetResponse from(PortfolioPhotoPageModel pageModel) {
    return PortfolioPhotoSearchGetResponse.builder()
        .photoList(
            pageModel.getPortfolioPhotoModelList().stream()
                .map(PortfolioPhotoSearchItemResponse::from)
                .toList())
        .isLast(pageModel.getIsLast())
        .build();
  }
}
