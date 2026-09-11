package com.web.gallery.config;

import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * application.ymlのCORSに関するプロパティを保持するConfigクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@RequiredArgsConstructor
@Getter
@ConfigurationProperties(prefix = "app.cors")
public class CorsConfig {
  /** 許可するオリジンのリスト */
  private final List<String> allowedOrigins;

  /**
   * 起動時にCORS許可オリジンの形式を検証する
   *
   * <p>環境変数 `FRONTEND_ORIGIN` に複数値・空白・カンマ区切りなどを誤設定すると、意図しないオリジンからの
   * 認証付きリクエストを許可してしまう。全プロファイル共通の防御として、ここで最低限の形式チェックを行う （本番プロファイルでの `https://` 限定・ワイルドカード禁止のチェックは
   * ProdConfigValidationRunner が担う）。
   *
   * @throws IllegalStateException 許可オリジンが未設定、または空白・カンマ・空白文字を含む場合
   */
  @PostConstruct
  void validateAllowedOrigins() {
    if (CollectionUtils.isEmpty(allowedOrigins)
        || allowedOrigins.stream().noneMatch(StringUtils::hasText)) {
      throw new IllegalStateException(
          "app.cors.allowed-origins（環境変数 FRONTEND_ORIGIN）を1件以上設定する必要があります");
    }
    for (String origin : allowedOrigins) {
      if (!StringUtils.hasText(origin) || origin.matches(".*[\\s,].*")) {
        throw new IllegalStateException(
            "app.cors.allowed-origins の各値は空白・カンマを含まない単一のオリジンである必要があります: [" + origin + "]");
      }
    }
  }
}
