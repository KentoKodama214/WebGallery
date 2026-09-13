package com.web.gallery.entity;

import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.enumeration.SortPhotoEnum;
import com.web.gallery.model.PhotoListFilterLogModel;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * 写真一覧絞り込みログテーブルのEntityクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
public class PhotoListFilterLog {
  /** 写真一覧絞り込みログNo */
  private Long photoListFilterLogNo;

  /** 写真アカウント番号（閲覧対象ギャラリーの所有者） */
  private Long photoAccountNo;

  /** 閲覧者のアカウント番号（未ログインの場合は0） */
  private Long accountNo;

  /** 向き区分 */
  private DirectionEnum directionKbn;

  /** お気に入り写真のみ絞り込みフラグ */
  private Boolean isFavorite;

  /** タグリスト（カンマ区切り） */
  private String tagList;

  /** 並び順 */
  private SortPhotoEnum sortBy;

  /** リファラ（取得できない場合は空文字） */
  private String referer;

  /** 送信元IPアドレス */
  private String ipAddress;

  /** 国（未解決時は空文字） */
  private String country;

  /** 地域（未解決時は空文字） */
  private String region;

  /** 作成者 */
  private Long createdBy;

  /** 作成日時 */
  private OffsetDateTime createdAt;

  /**
   * PhotoListFilterLogModelからPhotoListFilterLogエンティティを生成する
   *
   * @param model {@link PhotoListFilterLogModel}
   * @return {@link PhotoListFilterLog}
   */
  public static PhotoListFilterLog from(PhotoListFilterLogModel model) {
    return PhotoListFilterLog.builder()
        .photoAccountNo(model.getPhotoAccountNo().value())
        .accountNo(model.getAccountNo() != null ? model.getAccountNo().value() : 0L)
        .directionKbn(model.getDirectionKbn())
        .isFavorite(model.getIsFavoriteOnly().value())
        .tagList(model.getTagList())
        .sortBy(model.getSortBy())
        .referer(model.getReferer().value())
        .ipAddress(model.getIpAddress().value())
        .country(
            model.getGeoLocation().country() != null
                ? model.getGeoLocation().country().value()
                : "")
        .region(
            model.getGeoLocation().region() != null ? model.getGeoLocation().region().value() : "")
        .createdBy(model.getPhotoAccountNo().value())
        .build();
  }
}
