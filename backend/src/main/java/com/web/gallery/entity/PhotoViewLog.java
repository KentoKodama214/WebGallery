package com.web.gallery.entity;

import com.web.gallery.model.PhotoViewLogModel;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * 写真詳細閲覧ログテーブルのEntityクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
public class PhotoViewLog {
  /** 写真閲覧ログNo */
  private Long photoViewLogNo;

  /** 写真アカウント番号 */
  private Long photoAccountNo;

  /** 閲覧者のアカウント番号（未ログインの場合は0） */
  private Long accountNo;

  /** 写真番号 */
  private Long photoNo;

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
   * PhotoViewLogModelからPhotoViewLogエンティティを生成する
   *
   * @param model {@link PhotoViewLogModel}
   * @return {@link PhotoViewLog}
   */
  public static PhotoViewLog from(PhotoViewLogModel model) {
    return PhotoViewLog.builder()
        .photoAccountNo(model.getPhotoAccountNo().value())
        .accountNo(model.getAccountNo() != null ? model.getAccountNo().value() : 0L)
        .photoNo(model.getPhotoNo().value())
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
