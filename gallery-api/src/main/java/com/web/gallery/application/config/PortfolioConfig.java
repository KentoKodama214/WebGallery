package com.web.gallery.application.config;

/** ポートフォリオに関するプロパティを取得するポート */
public interface PortfolioConfig {

  /**
   * mini-userのポートフォリオ作成数上限を取得する
   *
   * @return mini-userのポートフォリオ作成数上限
   */
  Integer getMiniUserUpperLimit();

  /**
   * normal-userのポートフォリオ作成数上限を取得する
   *
   * @return normal-userのポートフォリオ作成数上限
   */
  Integer getNormalUserUpperLimit();

  /**
   * ポートフォリオ一覧の1ページあたりの表示件数を取得する
   *
   * @return ポートフォリオ一覧の1ページあたりの表示件数
   */
  Integer getPortfolioCountPerPage();
}
