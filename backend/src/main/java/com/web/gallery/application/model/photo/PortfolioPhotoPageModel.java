package com.web.gallery.application.model.photo;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** ポートフォリオに設定する写真の検索結果の1ページ分の情報を受け渡すためのModelクラス */
@Value
@Builder
public class PortfolioPhotoPageModel {
  /** 写真一覧（写真番号・タイトル・画像の署名付きURL・向きのみを持つ） */
  @NonNull private PortfolioPhotoModelList portfolioPhotoModelList;

  /** 最後のページかどうか */
  @NonNull private Boolean isLast;

  /**
   * PortfolioPhotoModelListと最後のページかどうかからPortfolioPhotoPageModelを生成する
   *
   * @param portfolioPhotoModelList {@link PortfolioPhotoModelList}
   * @param isLast 最後のページかどうか
   * @return {@link PortfolioPhotoPageModel}
   */
  public static PortfolioPhotoPageModel of(
      PortfolioPhotoModelList portfolioPhotoModelList, Boolean isLast) {
    return PortfolioPhotoPageModel.builder()
        .portfolioPhotoModelList(portfolioPhotoModelList)
        .isLast(isLast)
        .build();
  }
}
