package com.web.gallery.model;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** 写真のメタデータを含めた詳細情報を取得する時の情報を受け渡すためのModelクラス */
@Value
@Builder
public class PhotoDetailSearchModel {
  /** ログイン中のアカウントNo */
  private AccountNo accountNo;

  /** 写真のアカウントNo */
  @NonNull private AccountNo photoAccountNo;

  /** 写真番号 */
  @NonNull private PhotoNo photoNo;

  /**
   * PhotoDetailGetModelと写真のアカウント番号からPhotoDetailSearchModelを生成する
   *
   * @param photoDetailGetModel {@link PhotoDetailGetModel}
   * @param photoAccountNo 写真のアカウントNo
   * @return {@link PhotoDetailSearchModel}
   */
  public static PhotoDetailSearchModel of(
      PhotoDetailGetModel photoDetailGetModel, AccountNo photoAccountNo) {
    return PhotoDetailSearchModel.builder()
        .accountNo(photoDetailGetModel.getAccountNo())
        .photoAccountNo(photoAccountNo)
        .photoNo(photoDetailGetModel.getPhotoNo())
        .build();
  }
}
