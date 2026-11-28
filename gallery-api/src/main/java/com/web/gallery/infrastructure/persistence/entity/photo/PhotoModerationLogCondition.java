package com.web.gallery.infrastructure.persistence.entity.photo;

import lombok.Builder;
import lombok.Data;

/** 写真モデレーションログテーブルの抽出条件クラス */
@Data
@Builder
public class PhotoModerationLogCondition {
  /** 写真アカウント番号 */
  private Long photoAccountNo;

  /**
   * 写真アカウント番号で削除用の抽出条件を生成する
   *
   * @param photoAccountNo 写真アカウント番号
   * @return {@link PhotoModerationLogCondition}
   */
  public static PhotoModerationLogCondition byPhotoAccountNo(Long photoAccountNo) {
    return PhotoModerationLogCondition.builder().photoAccountNo(photoAccountNo).build();
  }
}
