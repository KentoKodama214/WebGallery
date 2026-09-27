package com.web.gallery.application.model.photo;

import com.web.gallery.domain.enumeration.DirectionEnum;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.Caption;
import com.web.gallery.domain.model.photo.FavoriteCount;
import com.web.gallery.domain.model.photo.ImageFilePath;
import com.web.gallery.domain.model.photo.IsFavorite;
import com.web.gallery.domain.model.photo.PhotoAt;
import com.web.gallery.domain.model.photo.PhotoNo;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** 写真の情報を受け渡すためのModelクラス */
@Value
@Builder(toBuilder = true)
public class PhotoModel {
  /** アカウント番号 */
  @NonNull private AccountNo accountNo;

  /** 写真番号 */
  @NonNull private PhotoNo photoNo;

  /** お気に入り数 */
  private FavoriteCount favoriteCount;

  /** お気に入り */
  private IsFavorite isFavorite;

  /** 撮影日時 */
  @NonNull private PhotoAt photoAt;

  /** 画像ファイルパス */
  @NonNull private ImageFilePath imageFilePath;

  /** キャプション */
  @NonNull private Caption caption;

  /**
   * 向き区分
   *
   * <p>{@link DirectionEnum}
   */
  @NonNull private DirectionEnum directionKbn;

  /** 写真タグリスト */
  @NonNull private PhotoTagModelList photoTagModelList;
}
