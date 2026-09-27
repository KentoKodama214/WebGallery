package com.web.gallery.application.model.photo;

import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.PhotoNo;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** 写真お気に入りの情報を受け渡すためのModelクラス */
@Value
@Builder
public class PhotoFavoriteModel {
  /** アカウント番号 */
  @NonNull private AccountNo accountNo;

  /** お気に入り写真アカウント番号 */
  @NonNull private AccountNo favoritePhotoAccountNo;

  /** 写真番号 */
  @NonNull private PhotoNo favoritePhotoNo;
}
