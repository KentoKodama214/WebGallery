package com.web.gallery.application.model.photo;

import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.common.IpAddress;
import com.web.gallery.domain.model.common.Referer;
import com.web.gallery.domain.model.photo.PhotoNo;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** 写真のメタデータを含めた詳細情報を取得するために必要な情報を受け渡すためのModelクラス */
@Value
@Builder
public class PhotoDetailGetModel {
  /** ログイン中のアカウントNo */
  private AccountNo accountNo;

  /** 写真のアカウントID */
  @NonNull private AccountId photoAccountId;

  /** 写真番号 */
  @NonNull private PhotoNo photoNo;

  /** 送信元IPアドレス（閲覧ログ記録用） */
  @NonNull private IpAddress ipAddress;

  /** リファラ（閲覧ログ記録用） */
  @NonNull private Referer referer;

  /**
   * ログイン中のアカウント番号、写真所有者のアカウントID、写真番号からPhotoDetailGetModelを生成する
   *
   * @param accountNo ログイン中のアカウントNo
   * @param photoAccountId 写真所有者のアカウントID
   * @param photoNo 写真番号
   * @param ipAddress 送信元IPアドレス
   * @param referer リファラ
   * @return {@link PhotoDetailGetModel}
   */
  public static PhotoDetailGetModel from(
      Long accountNo, String photoAccountId, Long photoNo, IpAddress ipAddress, Referer referer) {
    return PhotoDetailGetModel.builder()
        .accountNo(accountNo != null ? new AccountNo(accountNo) : null)
        .photoAccountId(new AccountId(photoAccountId))
        .photoNo(new PhotoNo(photoNo))
        .ipAddress(ipAddress)
        .referer(referer)
        .build();
  }
}
