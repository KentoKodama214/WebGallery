package com.web.gallery.model;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.IpAddress;
import com.web.gallery.domain.common.IpGeoLocation;
import com.web.gallery.domain.common.Referer;
import com.web.gallery.domain.photo.IsFavoriteOnly;
import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.enumeration.SortPhotoEnum;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * 写真一覧の絞り込み・並び替えログを受け渡すためのModelクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@Builder
public class PhotoListFilterLogModel {
  /** 写真アカウント番号（閲覧対象ギャラリーの所有者） */
  @NonNull private AccountNo photoAccountNo;

  /** 向き区分 */
  @NonNull private DirectionEnum directionKbn;

  /** お気に入り写真のみ絞り込みフラグ */
  @NonNull private IsFavoriteOnly isFavoriteOnly;

  /** タグリスト（カンマ区切り） */
  @NonNull private String tagList;

  /** 並び順 */
  @NonNull private SortPhotoEnum sortBy;

  /** リファラ */
  @NonNull private Referer referer;

  /** 送信元IPアドレス */
  @NonNull private IpAddress ipAddress;

  /** 国・地域 */
  @NonNull private IpGeoLocation geoLocation;
}
