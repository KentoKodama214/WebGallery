package com.web.gallery.config;

import com.maxmind.geoip2.DatabaseReader;
import java.io.File;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * GeoLite2（MaxMind）データベースリーダーのBean定義クラス
 *
 * <p>{@link GeoIpConfig} で指定されたファイルが存在しない場合はBeanを生成しない。 呼び出し側（{@link
 * com.web.gallery.helper.GeoIpResolver}）はBean不在を許容し、常に空値（未解決）を返す。
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class GeoIpReaderConfig {

  private final GeoIpConfig geoIpConfig;

  /**
   * GeoLite2データベースリーダーを生成する
   *
   * @return {@link DatabaseReader}（データベースファイルが存在しない場合はnull）
   */
  @Bean
  public DatabaseReader geoIpDatabaseReader() {
    String databasePath = geoIpConfig.getDatabasePath();
    if (!StringUtils.hasText(databasePath)) {
      log.warn("GeoIP database path is not configured. Country/region resolution is disabled.");
      return null;
    }

    File databaseFile = new File(databasePath);
    if (!databaseFile.isFile()) {
      log.warn(
          "GeoIP database file not found. Country/region resolution is disabled. (path: {})",
          databasePath);
      return null;
    }

    try {
      return new DatabaseReader.Builder(databaseFile).build();
    } catch (IOException e) {
      log.warn(
          "Failed to load GeoIP database. Country/region resolution is disabled. (path: {})",
          databasePath,
          e);
      return null;
    }
  }
}
