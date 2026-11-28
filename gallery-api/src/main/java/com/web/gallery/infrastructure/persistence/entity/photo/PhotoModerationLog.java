package com.web.gallery.infrastructure.persistence.entity.photo;

import com.web.gallery.application.aggregate.Photo;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

/** 写真モデレーションログテーブルのEntityクラス */
@Data
@Builder
public class PhotoModerationLog {
  /** 写真モデレーションログNo */
  private Long photoModerationLogNo;

  /** 写真アカウント番号 */
  private Long photoAccountNo;

  /** 写真番号 */
  private Long photoNo;

  /** 操作した管理者のアカウント番号 */
  private Long adminAccountNo;

  /** 非公開フラグ（trueなら非公開にした操作、falseなら非公開を解除した操作） */
  private Boolean isHidden;

  /** 理由 */
  private String reason;

  /** 作成者 */
  private Long createdBy;

  /** 作成日時 */
  private OffsetDateTime createdAt;

  /**
   * 公開状態の変更内容を設定済みの写真集約から写真モデレーションログエンティティを生成する
   *
   * @param photo {@link Photo}（{@link Photo#forModeration}で生成し、変更内容を設定済みのもの）
   * @return {@link PhotoModerationLog}
   */
  public static PhotoModerationLog from(Photo photo) {
    return PhotoModerationLog.builder()
        .photoAccountNo(photo.getAccountNo().value())
        .photoNo(photo.getPhotoNo().value())
        .adminAccountNo(photo.getModeratorAccountNo().value())
        .isHidden(photo.getHiddenByAdmin().value())
        .reason(photo.getModerationReason().value())
        .createdBy(photo.getModeratorAccountNo().value())
        .build();
  }
}
