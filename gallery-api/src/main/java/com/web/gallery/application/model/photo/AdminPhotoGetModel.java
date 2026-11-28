package com.web.gallery.application.model.photo;

import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.photo.IsHiddenByAdmin;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** 管理者用の写真一覧をDBから取得する際の抽出・ページング情報を受け渡すためのModelクラス */
@Value
@Builder
public class AdminPhotoGetModel {
  /** 写真所有者のアカウントIDによる絞り込み（任意） */
  private AccountId accountId;

  /** 管理者非公開フラグによる絞り込み（任意） */
  private IsHiddenByAdmin isHiddenByAdmin;

  /** 取得件数上限（最後のページかどうかの判定用に、1ページあたりの表示件数より1件多く取得する） */
  @NonNull private Integer limit;

  /** 取得開始位置（0始まり） */
  @NonNull private Integer offset;

  /**
   * AdminPhotoListGetModelと1ページあたりの表示件数からAdminPhotoGetModelを生成する
   *
   * @param adminPhotoListGetModel {@link AdminPhotoListGetModel}
   * @param photoCountPerPage 1ページあたりの表示件数
   * @return {@link AdminPhotoGetModel}
   */
  public static AdminPhotoGetModel of(
      AdminPhotoListGetModel adminPhotoListGetModel, Integer photoCountPerPage) {
    return AdminPhotoGetModel.builder()
        .accountId(adminPhotoListGetModel.getAccountId())
        .isHiddenByAdmin(adminPhotoListGetModel.getIsHiddenByAdmin())
        .limit(photoCountPerPage + 1)
        .offset((adminPhotoListGetModel.getPageNo() - 1) * photoCountPerPage)
        .build();
  }
}
