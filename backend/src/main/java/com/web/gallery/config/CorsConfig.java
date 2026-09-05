package com.web.gallery.config;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
}
