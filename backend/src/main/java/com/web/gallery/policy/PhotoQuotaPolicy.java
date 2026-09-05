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
}
