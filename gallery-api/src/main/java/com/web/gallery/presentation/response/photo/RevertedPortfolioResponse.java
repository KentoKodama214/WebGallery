package com.web.gallery.presentation.response.photo;

import com.web.gallery.application.model.photo.PortfolioModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/** 写真の削除に伴い公開から下書きに戻したポートフォリオのレスポンスパラメータを保持するクラス */
@Schema(description = "写真削除に伴い下書きに戻したポートフォリオ")
@Data
@Builder
public class RevertedPortfolioResponse {
  /** ポートフォリオ番号 */
  @Schema(description = "ポートフォリオ番号", example = "1")
  private Long portfolioNo;

  /** テーマ */
  @Schema(description = "テーマ", example = "日本の自然")
  private String theme;

  /**
   * PortfolioModelからRevertedPortfolioResponseを生成する
   *
   * @param model {@link PortfolioModel}
   * @return {@link RevertedPortfolioResponse}
   */
  public static RevertedPortfolioResponse from(PortfolioModel model) {
    return RevertedPortfolioResponse.builder()
        .portfolioNo(model.getPortfolioNo().value())
        .theme(model.getTheme().value())
        .build();
  }
}
