package com.web.gallery.domain.model.photo;

import java.io.Serializable;

/**
 * 権限区分ごとの写真登録枚数上限の複合値オブジェクト
 *
 * @param miniUserUpperLimit MINI権限の登録枚数上限
 * @param normalUserUpperLimit NORMAL権限の登録枚数上限
 */
public record PhotoUpperLimits(Integer miniUserUpperLimit, Integer normalUserUpperLimit)
    implements Serializable {

  /**
   * 全プロパティが未設定のPhotoUpperLimitsを生成する
   *
   * @return {@link PhotoUpperLimits}
   */
  public static PhotoUpperLimits empty() {
    return new PhotoUpperLimits(null, null);
  }
}
