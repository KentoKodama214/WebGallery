package com.web.gallery.application.config;

/** ログインに関するプロパティを取得するポート */
public interface LoginConfig {

  /** ログイン失敗上限回数を取得する */
  Integer getFailCount();

  /**
   * アカウントロックの自動解除までの経過時間（分）を取得する
   *
   * <p>最後のアカウント行更新（＝直近のログイン失敗）からこの時間が経過していれば、 次回ログイン／リフレッシュ時にログイン失敗回数を0にリセットしてロックを解除する。
   * 総当たり攻撃中は失敗のたびに更新時刻が進むためロックは維持される。
   */
  Integer getLockDurationMinutes();
}
