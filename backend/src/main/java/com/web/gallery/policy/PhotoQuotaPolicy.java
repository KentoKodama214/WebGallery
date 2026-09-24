package com.web.gallery.policy;

import com.web.gallery.config.PhotoConfig;
import com.web.gallery.domain.photo.PhotoCount;
import com.web.gallery.enumeration.AuthorityEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 写真の登録枚数上限に関するビジネスルールを判定するドメインサービス */
@Component
@RequiredArgsConstructor
public class PhotoQuotaPolicy {

  private final PhotoConfig photoConfig;

  /**
   * アカウントの権限区分と現在の写真登録枚数から、登録枚数が上限に達しているかどうかを判定する
   *
   * @param authorityKbn アカウントの権限区分
   * @param currentCount 現在の写真登録枚数
   * @return 上限に達している場合、true
   */
  public Boolean isReached(AuthorityEnum authorityKbn, PhotoCount currentCount) {
    switch (authorityKbn) {
      case MINI:
        return currentCount.value() > (photoConfig.getMiniUserUpperLimit() - 1);
      case NORMAL:
        return currentCount.value() > (photoConfig.getNormalUserUpperLimit() - 1);
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
   * @return 登録後の枚数が上限を超える場合、true
   */
  public Boolean isReached(
      AuthorityEnum authorityKbn, PhotoCount currentCount, PhotoCount requestedCount) {
    return isReached(
        authorityKbn, new PhotoCount(currentCount.value() + requestedCount.value() - 1));
  }

  /**
   * アカウントの権限区分と現在の写真登録枚数から、残り登録可能枚数を判定する
   *
   * @param authorityKbn アカウントの権限区分
   * @param currentCount 現在の写真登録枚数
   * @return 残り登録可能枚数。上限が存在しない権限区分の場合はnull
   */
  public Integer remainingCount(AuthorityEnum authorityKbn, PhotoCount currentCount) {
    switch (authorityKbn) {
      case MINI:
        return Math.max(0, photoConfig.getMiniUserUpperLimit() - currentCount.value());
      case NORMAL:
        return Math.max(0, photoConfig.getNormalUserUpperLimit() - currentCount.value());
      case SPECIAL:
      case ADMINISTRATOR:
        return null;
      default:
        return 0;
    }
  }
}
