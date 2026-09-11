package com.web.gallery.config;

import java.time.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.ymlのレート制限に関するプロパティを保持するConfigクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@RequiredArgsConstructor
@Getter
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitConfig {

  /** レート制限を有効にするかどうか（テスト等で無効化できるようにする） */
  private final boolean enabled;

  /** ログインエンドポイントのしきい値（総当たり対策で低め） */
  private final Bucket auth;

  /** アカウント登録エンドポイントのしきい値 */
  private final Bucket register;

  /** 上記以外の `/api/**` リクエストのしきい値 */
  private final Bucket general;

  /**
   * 1カテゴリ分の固定ウィンドウしきい値
   *
   * @param capacity ウィンドウ内で許可する最大リクエスト数
   * @param windowSeconds ウィンドウの長さ（秒）
   */
  public record Bucket(int capacity, int windowSeconds) {

    /**
     * ウィンドウの長さを {@link Duration} で返す
     *
     * @return ウィンドウの長さ
     */
    public Duration window() {
      return Duration.ofSeconds(windowSeconds);
    }
  }
}
