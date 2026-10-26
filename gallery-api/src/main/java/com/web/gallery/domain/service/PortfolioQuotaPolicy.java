package com.web.gallery.domain.service;

import com.web.gallery.domain.enumeration.AuthorityEnum;
import com.web.gallery.domain.model.photo.PortfolioCount;
import com.web.gallery.domain.model.photo.PortfolioUpperLimits;
import org.springframework.stereotype.Component;

/** ポートフォリオの作成数上限に関するビジネスルールを判定するドメインサービス */
@Component
public class PortfolioQuotaPolicy {

  /**
   * アカウントの権限区分と現在のポートフォリオ作成数から、作成数が上限に達しているかどうかを判定する
   *
   * <p>special-user・administratorは上限を持たない
   *
   * @param authorityKbn アカウントの権限区分
   * @param currentCount 現在のポートフォリオ作成数（下書きを含む）
   * @param upperLimits 権限区分ごとの作成数上限
   * @return 上限に達している（これ以上作成できない）場合、true
   */
  public Boolean isReached(
      AuthorityEnum authorityKbn, PortfolioCount currentCount, PortfolioUpperLimits upperLimits) {
    switch (authorityKbn) {
      case MINI:
        return currentCount.value() >= upperLimits.miniUserUpperLimit();
      case NORMAL:
        return currentCount.value() >= upperLimits.normalUserUpperLimit();
      case SPECIAL:
      case ADMINISTRATOR:
        return false;
      default:
        return true;
    }
  }
}
