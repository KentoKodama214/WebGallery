package com.web.gallery.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.ymlのGeoIP（IPアドレスから国・地域を解決）に関するプロパティを保持するConfigクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@RequiredArgsConstructor
@Getter
@ConfigurationProperties(prefix = "app.geoip")
public class GeoIpConfig {

  /**
   * GeoLite2（MaxMind）データベースファイルの絶対パス
   *
   * <p>ライセンスの関係でWARには同梱せず、サーバー上の外部ファイルを参照する。月次更新される想定のため
   * リビルド不要。ファイルが存在しない場合は起動を止めず、解決処理は常に空値（未解決）を返す。
   */
  private final String databasePath;
}
