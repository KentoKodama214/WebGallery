package com.web.gallery.application.model.photo;

import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.photo.IsHiddenByAdmin;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** 管理者用の写真一覧を取得するために必要な情報を受け渡すためのModelクラス */
@Value
@Builder
public class AdminPhotoListGetModel {
  /** 写真所有者のアカウントIDによる絞り込み（任意） */
  private AccountId accountId;

  /** 管理者非公開フラグによる絞り込み（任意） */
  private IsHiddenByAdmin isHiddenByAdmin;

  /** ページ番号 */
  @NonNull private Integer pageNo;
}
