package com.web.gallery.application.model.photo;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** 本人のポートフォリオ一覧の1ページ分の情報を受け渡すためのModelクラス */
@Value
@Builder
public class PortfolioPageModel {
  /** ポートフォリオ一覧（表示順） */
  @NonNull private PortfolioModelList portfolioModelList;

  /** 最後のページかどうか */
  @NonNull private Boolean isLast;

  /**
   * PortfolioModelListと最後のページかどうかからPortfolioPageModelを生成する
   *
   * @param portfolioModelList {@link PortfolioModelList}
   * @param isLast 最後のページかどうか
   * @return {@link PortfolioPageModel}
   */
  public static PortfolioPageModel of(PortfolioModelList portfolioModelList, Boolean isLast) {
    return PortfolioPageModel.builder()
        .portfolioModelList(portfolioModelList)
        .isLast(isLast)
        .build();
  }
}
