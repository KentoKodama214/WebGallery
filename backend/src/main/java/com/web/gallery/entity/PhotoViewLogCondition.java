package com.web.gallery.entity;

import lombok.Builder;
import lombok.Data;

/** 写真詳細閲覧ログテーブルの抽出条件クラス */
@Data
@Builder
public class PhotoViewLogCondition {
  /** 写真アカウント番号 */
  private Long photoAccountNo;

  /**
   * 写真アカウント番号で削除用の抽出条件を生成する
   *
   * @param photoAccountNo 写真アカウント番号
   * @return {@link PhotoViewLogCondition}
   */
  public static PhotoViewLogCondition byPhotoAccountNo(Long photoAccountNo) {
    return PhotoViewLogCondition.builder().photoAccountNo(photoAccountNo).build();
  }
}
