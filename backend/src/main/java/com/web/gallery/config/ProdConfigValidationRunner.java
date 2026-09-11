package com.web.gallery.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 本番プロファイル（{@code prod}）専用の設定検証を起動時に実行するランナー
 *
 * <p>誤って開発向けの緩い設定のまま本番デプロイされると、API仕様の露出・平文通信・CORSの緩和などの 脆弱な状態で公開されてしまう。ここで危険な構成を検出したら {@link
 * IllegalStateException} を投げて アプリケーションの起動自体を失敗させる（フェイルクローズ）。
 *
 * <p>{@code prod} 以外のプロファイルではBeanが生成されないため、ローカル開発・テストには影響しない。
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class ProdConfigValidationRunner implements ApplicationRunner {

  private final CorsConfig corsConfig;

  private final S3Config s3Config;

  /**
   * 起動完了時に本番設定の検証を実行する
   *
   * @param args アプリケーション引数（未使用）
   */
  @Override
  public void run(ApplicationArguments args) {
    validate();
    log.info("本番プロファイルの設定検証が完了しました。");
  }

  /**
   * 本番プロファイルで満たすべき設定条件を検証する
   *
   * @throws IllegalStateException いずれかの条件を満たさない場合
   */
  void validate() {
    validateCorsAllowedOrigins(corsConfig.getAllowedOrigins());
    validateHttpsUrl("app.s3.endpoint", s3Config.getEndpoint());
    validateHttpsUrl("app.s3.public-base-url", s3Config.getPublicBaseUrl());
  }

  /**
   * CORS許可オリジンが本番として妥当か（1件以上・すべてHTTPSの絶対オリジン・ワイルドカード無し）を検証する
   *
   * @param allowedOrigins {@code app.cors.allowed-origins} の値
   * @throws IllegalStateException 妥当でない場合
   */
  private void validateCorsAllowedOrigins(List<String> allowedOrigins) {
    if (CollectionUtils.isEmpty(allowedOrigins)
        || allowedOrigins.stream().noneMatch(StringUtils::hasText)) {
      throw new IllegalStateException(
          "本番プロファイルでは app.cors.allowed-origins（環境変数 FRONTEND_ORIGIN）を1件以上設定する必要があります");
    }

    for (String origin : allowedOrigins) {
      if (!StringUtils.hasText(origin)) {
        throw new IllegalStateException("本番プロファイルの app.cors.allowed-origins に空の値を含めることはできません");
      }
      if (origin.contains("*")) {
        throw new IllegalStateException(
            "本番プロファイルの app.cors.allowed-origins にワイルドカード（*）を指定することはできません: " + origin);
      }
      URI uri = parseUri("app.cors.allowed-origins", origin);
      if (!"https".equalsIgnoreCase(uri.getScheme()) || !StringUtils.hasText(uri.getHost())) {
        throw new IllegalStateException(
            "本番プロファイルの app.cors.allowed-origins は https:// のオリジンである必要があります: " + origin);
      }
      String path = uri.getRawPath();
      if ((path != null && !path.isEmpty() && !"/".equals(path))
          || uri.getRawQuery() != null
          || uri.getRawFragment() != null) {
        throw new IllegalStateException(
            "本番プロファイルの app.cors.allowed-origins はパス・クエリを含まないオリジンのみ指定できます: " + origin);
      }
    }
  }

  /**
   * 値が設定されている場合に、それがHTTPSのURLであることを検証する（未設定は許容）
   *
   * @param propertyName 検証対象のプロパティ名（エラーメッセージ用）
   * @param value 検証対象の値
   * @throws IllegalStateException 値が設定されているのにHTTPSでない場合
   */
  private void validateHttpsUrl(String propertyName, String value) {
    if (!StringUtils.hasText(value)) {
      return;
    }
    URI uri = parseUri(propertyName, value);
    if (!"https".equalsIgnoreCase(uri.getScheme())) {
      throw new IllegalStateException(
          "本番プロファイルの " + propertyName + " は https:// である必要があります（平文通信の禁止）: " + value);
    }
  }

  /**
   * 文字列をURIとしてパースする
   *
   * @param propertyName 対象プロパティ名（エラーメッセージ用）
   * @param value パース対象の文字列
   * @return パース結果の {@link URI}
   * @throws IllegalStateException URIとして不正な場合
   */
  private URI parseUri(String propertyName, String value) {
    try {
      return new URI(value.trim());
    } catch (URISyntaxException e) {
      throw new IllegalStateException("本番プロファイルの " + propertyName + " がURLとして不正です: " + value, e);
    }
  }
}
