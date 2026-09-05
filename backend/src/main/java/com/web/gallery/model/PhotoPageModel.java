package com.web.gallery.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** 写真一覧の1ページ分の情報を受け渡すためのModelクラス */
@Value
@Builder
public class PhotoPageModel {
  /** 写真一覧 */
  @NonNull private PhotoModelList photoModelList;

  /** 最後のページかどうか */
  @NonNull private Boolean isLast;

  /**
   * PhotoModelListと最後のページかどうかからPhotoPageModelを生成する
   *
   * @param photoModelList {@link PhotoModelList}
   * @param isLast 最後のページかどうか
   * @return {@link PhotoPageModel}
   */
  public static PhotoPageModel of(PhotoModelList photoModelList, Boolean isLast) {
    return PhotoPageModel.builder().photoModelList(photoModelList).isLast(isLast).build();
  }
}
