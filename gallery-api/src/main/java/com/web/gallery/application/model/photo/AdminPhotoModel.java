package com.web.gallery.application.model.photo;

import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountName;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.ImageFilePath;
import com.web.gallery.domain.model.photo.IsHiddenByAdmin;
import com.web.gallery.domain.model.photo.PhotoJapaneseTitle;
import com.web.gallery.domain.model.photo.PhotoNo;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** 管理者用写真一覧の1件分の情報を受け渡すためのModelクラス */
@Value
@Builder(toBuilder = true)
public class AdminPhotoModel {
  /** 写真所有者のアカウント番号 */
  @NonNull private AccountNo accountNo;

  /** 写真所有者のアカウントID */
  @NonNull private AccountId accountId;

  /** 写真所有者のアカウント名 */
  @NonNull private AccountName accountName;

  /** 写真番号 */
  @NonNull private PhotoNo photoNo;

  /** 写真タイトル日本語名 */
  @NonNull private PhotoJapaneseTitle photoJapaneseTitle;

  /** 画像ファイルパス */
  @NonNull private ImageFilePath imageFilePath;

  /** 管理者非公開フラグ */
  @NonNull private IsHiddenByAdmin isHiddenByAdmin;

  /** 登録日時 */
  @NonNull private OffsetDateTime createdAt;
}
