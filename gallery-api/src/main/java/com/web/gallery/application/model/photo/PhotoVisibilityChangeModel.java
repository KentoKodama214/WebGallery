package com.web.gallery.application.model.photo;

import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.IsHiddenByAdmin;
import com.web.gallery.domain.model.photo.ModerationReason;
import com.web.gallery.domain.model.photo.PhotoNo;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** 管理者による写真の公開状態変更（非公開・非公開解除）に必要な情報を受け渡すためのModelクラス */
@Value
@Builder
public class PhotoVisibilityChangeModel {
  /** 写真所有者のアカウント番号 */
  @NonNull private AccountNo photoAccountNo;

  /** 写真番号 */
  @NonNull private PhotoNo photoNo;

  /** 操作する管理者のアカウント番号 */
  @NonNull private AccountNo adminAccountNo;

  /** 変更後の管理者非公開フラグ（非公開にする場合はtrue、非公開を解除する場合はfalse） */
  @NonNull private IsHiddenByAdmin isHiddenByAdmin;

  /** 理由 */
  @NonNull private ModerationReason reason;
}
