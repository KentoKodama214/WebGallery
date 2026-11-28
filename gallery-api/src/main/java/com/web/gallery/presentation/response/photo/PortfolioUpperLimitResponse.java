package com.web.gallery.presentation.response.photo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/** ポートフォリオ作成上限チェックのレスポンスパラメータを保持するクラス */
@Schema(description = "ポートフォリオ作成上限チェックレスポンス")
@Data
@Builder
public class PortfolioUpperLimitResponse {
  /** ポートフォリオの作成数が上限に達しているか */
  @Schema(description = "ポートフォリオの作成数（下書きを含む）が上限に達しているか")
  private Boolean isReachedUpperLimit;

  /**
   * レスポンスを生成する
   *
   * @param isReachedUpperLimit ポートフォリオの作成数が上限に達しているか
   * @return {@link PortfolioUpperLimitResponse}
   */
  public static PortfolioUpperLimitResponse of(Boolean isReachedUpperLimit) {
    return PortfolioUpperLimitResponse.builder().isReachedUpperLimit(isReachedUpperLimit).build();
  }
}
