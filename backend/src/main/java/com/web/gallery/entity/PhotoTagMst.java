package com.web.gallery.entity;

import com.web.gallery.model.PhotoTagModel;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

/** 写真タグマスタテーブルのEntityクラス */
@Data
@Builder
public class PhotoTagMst {
  /** ID */
  private Long id;

  /** アカウント番号 */
  private Long accountNo;

  /** 写真番号 */
  private Long photoNo;

  /** タグ番号 */
  private Long tagNo;

  /** 作成者 */
  private Long createdBy;

  /** 作成日時 */
  private OffsetDateTime createdAt;

  /** タグ日本語名 */
  private String tagJapaneseName;

  /** タグ英語名 */
  private String tagEnglishName;

  /**
   * PhotoTagModelからPhotoTagMstエンティティを生成する
   *
   * @param model {@link PhotoTagModel}
   * @return {@link PhotoTagMst}
   */
  public static PhotoTagMst from(PhotoTagModel model) {
    return PhotoTagMst.builder()
        .accountNo(model.getAccountNo().value())
        .photoNo(model.getPhotoNo() != null ? model.getPhotoNo().value() : null)
        .tagNo(model.getTagNo() != null ? model.getTagNo().value() : null)
        .createdBy(model.getAccountNo().value())
        .tagJapaneseName(model.getTagJapaneseName().value())
        .tagEnglishName(model.getTagEnglishName().value())
        .build();
  }
}
