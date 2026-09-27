package com.web.gallery.application.model.photo;

import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.ImageFilePath;
import com.web.gallery.domain.model.photo.PhotoNo;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** 写真を削除するときの情報を受け渡すためのModelクラス */
@Value
@Builder
public class PhotoDeleteModel {
  /** アカウント番号 */
  @NonNull private AccountNo accountNo;

  /** 写真番号 */
  @NonNull private PhotoNo photoNo;

  /** 画像ファイルパス */
  @NonNull private ImageFilePath imageFilePath;
}
