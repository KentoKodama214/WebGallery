package com.web.gallery.application.model.photo;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** 管理者用写真一覧の1ページ分の情報を受け渡すためのModelクラス */
@Value
@Builder
public class AdminPhotoPageModel {
  /** 写真一覧 */
  @NonNull private AdminPhotoModelList adminPhotoModelList;

  /** 最後のページかどうか */
  @NonNull private Boolean isLast;

  /**
   * AdminPhotoModelListと最後のページかどうかからAdminPhotoPageModelを生成する
   *
   * @param adminPhotoModelList {@link AdminPhotoModelList}
   * @param isLast 最後のページかどうか
   * @return {@link AdminPhotoPageModel}
   */
  public static AdminPhotoPageModel of(AdminPhotoModelList adminPhotoModelList, Boolean isLast) {
    return AdminPhotoPageModel.builder()
        .adminPhotoModelList(adminPhotoModelList)
        .isLast(isLast)
        .build();
  }
}
