package com.web.gallery.entity;

import lombok.Builder;
import lombok.Data;

/** 写真一覧絞り込みログテーブルの抽出条件クラス */
@Data
@Builder
public class PhotoListFilterLogCondition {
  /** 写真アカウント番号 */
  private Long photoAccountNo;

  /**
   * 写真アカウント番号で削除用の抽出条件を生成する
   *
   * @param photoAccountNo 写真アカウント番号
   * @return {@link PhotoListFilterLogCondition}
   */
  public static PhotoListFilterLogCondition byPhotoAccountNo(Long photoAccountNo) {
    return PhotoListFilterLogCondition.builder().photoAccountNo(photoAccountNo).build();
  }
}
