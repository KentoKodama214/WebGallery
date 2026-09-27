package com.web.gallery.domain.service;

import com.web.gallery.domain.enumeration.AuthorityEnum;
import com.web.gallery.domain.model.photo.PhotoCount;
import com.web.gallery.domain.model.photo.PhotoUpperLimits;
import org.springframework.stereotype.Component;

/** 写真の登録枚数上限に関するビジネスルールを判定するドメインサービス */
@Component
public class PhotoQuotaPolicy {

  /**
   * アカウントの権限区分と現在の写真登録枚数から、登録枚数が上限に達しているかどうかを判定する
   *
   * @param authorityKbn アカウントの権限区分
   * @param currentCount 現在の写真登録枚数
   * @param upperLimits 権限区分ごとの登録枚数上限
   * @return 上限に達している場合、true
   */
  public Boolean isReached(
      AuthorityEnum authorityKbn, PhotoCount currentCount, PhotoUpperLimits upperLimits) {
    switch (authorityKbn) {
      case MINI:
        return currentCount.value() > (upperLimits.miniUserUpperLimit() - 1);
      case NORMAL:
        return currentCount.value() > (upperLimits.normalUserUpperLimit() - 1);
      case SPECIAL:
      case ADMINISTRATOR:
        return false;
      default:
        return true;
    }
  }

  /**
   * アカウントの権限区分・現在の写真登録枚数・今回新規登録しようとしている枚数から、登録後の枚数が上限を超えるかどうかを判定する
   *
   * <p>複数枚の一括登録において、1件も登録処理を行う前に全体の枚数で上限超過を判定するために用いる
   *
   * @param authorityKbn アカウントの権限区分
   * @param currentCount 現在の写真登録枚数
   * @param requestedCount 今回新規登録しようとしている枚数
   * @param upperLimits 権限区分ごとの登録枚数上限
   * @return 登録後の枚数が上限を超える場合、true
   */
  public Boolean isReached(
      AuthorityEnum authorityKbn,
      PhotoCount currentCount,
      PhotoCount requestedCount,
      PhotoUpperLimits upperLimits) {
    return isReached(
        authorityKbn,
        new PhotoCount(currentCount.value() + requestedCount.value() - 1),
        upperLimits);
  }

  /**
   * アカウントの権限区分と現在の写真登録枚数から、残り登録可能枚数を判定する
   *
   * @param authorityKbn アカウントの権限区分
   * @param currentCount 現在の写真登録枚数
   * @param upperLimits 権限区分ごとの登録枚数上限
   * @return 残り登録可能枚数。上限が存在しない権限区分の場合はnull
   */
  public Integer remainingCount(
      AuthorityEnum authorityKbn, PhotoCount currentCount, PhotoUpperLimits upperLimits) {
    switch (authorityKbn) {
      case MINI:
        return Math.max(0, upperLimits.miniUserUpperLimit() - currentCount.value());
      case NORMAL:
        return Math.max(0, upperLimits.normalUserUpperLimit() - currentCount.value());
      case SPECIAL:
      case ADMINISTRATOR:
        return null;
      default:
        return 0;
    }
  }
}
