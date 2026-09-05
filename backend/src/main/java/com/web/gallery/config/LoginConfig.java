package com.web.gallery.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.ymlのログインに関するプロパティを保持するConfigクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@RequiredArgsConstructor
@Getter
@ConfigurationProperties(prefix = "auth.login")
public class LoginConfig {
  /** ログイン失敗上限回数 */
  private final Integer failCount;

  /**
   * アカウントロックの自動解除までの経過時間（分）
   *
   * <p>最後のアカウント行更新（＝直近のログイン失敗）からこの時間が経過していれば、 次回ログイン／リフレッシュ時にログイン失敗回数を0にリセットしてロックを解除する。
   * 総当たり攻撃中は失敗のたびに更新時刻が進むためロックは維持される。
   */
  private final Integer lockDurationMinutes;
}
