package com.web.gallery.infrastructure.config;

import com.web.gallery.infrastructure.web.CorsConfig;
import com.web.gallery.infrastructure.web.TrustedProxyConfig;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
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

  /** {@code X-Forwarded-For}を信頼する送信元範囲のプロパティキー */
  private static final String TRUSTED_PROXIES_PROPERTY = "app.forwarded.trusted-proxies";

  /**
   * 信頼プロキシ範囲の正規表現が広すぎないかを確かめるための、グローバルに到達可能なIPの例
   *
   * <p>正規表現の表記は無数にあるため（{@code .*} / {@code (.*)} / {@code ^.*$} / {@code [0-9.]*} 等）、
   * 文字列の照合では網羅できない。実際にコンパイルしてこれらに一致するかどうかで判定する
   *
   * <p><b>この判定の限界</b>：あくまで「catch-allに近い値」を弾くための固定プローブであり、過不足の両方がある。
   *
   * <ul>
   *   <li>過検出：{@code 203.0.113.1}はTEST-NET-3（RFC5737）だが、前段プロキシを同レンジに置いた検証環境では 正当な設定でも起動に失敗する
   *   <li>検出漏れ：{@code 10\.\d+\.\d+\.\d+}のようにプライベート範囲全域を許す値はどのプローブにも一致しない。
   *       「前段プロキシのCIDRだけに狭める」という本来の目的は、この検証だけでは担保できない
   * </ul>
   *
   * <p>より強く担保するなら、設定を正規表現ではなくCIDRのリスト（{@code IpAddressMatcher}）に変え、
   * プレフィックス長の下限（例：/16未満を拒否）を検証する形にする必要がある
   */
  private static final List<String> GLOBAL_IP_PROBES =
      List.of("8.8.8.8", "203.0.113.1", "2001:db8::1");

  private final CorsConfig corsConfig;

  private final S3Config s3Config;

  private final DataSourceProperties dataSourceProperties;

  private final DataSourceReplicaConfig dataSourceReplicaConfig;

  private final TrustedProxyConfig trustedProxyConfig;

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
    validateReplicaUrl(dataSourceReplicaConfig.getUrl());
    validateTrustedProxies(trustedProxyConfig.getTrustedProxies());
  }

  /**
   * {@code X-Forwarded-For}を信頼する送信元の範囲が、本番として妥当に絞られているかを検証する
   *
   * <p>この範囲が広いと、範囲内から届いた{@code X-Forwarded-For}をそのまま信頼してしまい、送信元IPの詐称
   * （レート制限の回避・ログイン履歴やアクセスログの偽装）が成立する。{@code application-prod.yml}は
   * 環境変数プレースホルダに空文字の既定値を与えており、未設定・空文字・不正な正規表現・広すぎる正規表現の いずれもここで検出して起動を失敗させる。空文字既定にしているのは、環境変数の設定漏れを
   * Springの{@code Could not resolve placeholder}ではなく「前段プロキシのCIDRを明示設定する必要があります」という
   * 運用者向けのメッセージで伝えるため（検証の実行経路もこのメソッドに一本化される）。
   *
   * <p>広すぎるかどうかは、値を実際に{@link Pattern}へコンパイルし{@link #GLOBAL_IP_PROBES}に 一致するかどうかで判定する。文字列の照合では{@code
   * .*}と等価な無数の表記を網羅できないため。 正規表現として不正な値も、この時点で検出する（不正なままだと {@code
   * ForwardedForFilter}が「どの送信元も信頼しない」に倒れ、実クライアントIPを 一切復元できなくなる）。
   *
   * @param trustedProxies {@value #TRUSTED_PROXIES_PROPERTY}（環境変数 {@code TRUSTED_PROXIES}）の値
   * @throws IllegalStateException 未設定、正規表現として不正、またはグローバルIPに一致する場合
   */
  private void validateTrustedProxies(String trustedProxies) {
    if (!StringUtils.hasText(trustedProxies)) {
      throw new IllegalStateException(
          "本番プロファイルでは "
              + TRUSTED_PROXIES_PROPERTY
              + "（環境変数 TRUSTED_PROXIES）に前段プロキシのCIDRを明示設定する必要があります");
    }

    Pattern pattern;
    try {
      pattern = Pattern.compile(trustedProxies.trim());
    } catch (PatternSyntaxException e) {
      throw new IllegalStateException(
          TRUSTED_PROXIES_PROPERTY + " は正規表現として不正です: " + trustedProxies, e);
    }

    for (String probe : GLOBAL_IP_PROBES) {
      if (pattern.matcher(probe).matches()) {
        throw new IllegalStateException(
            "本番プロファイルの "
                + TRUSTED_PROXIES_PROPERTY
                + " がグローバルIP（"
                + probe
                + "）にも一致します。前段プロキシのCIDRだけに狭めてください"
                + "（送信元IPの詐称を許してしまいます）: "
                + trustedProxies);
      }
    }
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
   * リードレプリカの接続URLが設定されている場合に、プライマリと異なるPostgreSQL接続URLであることを検証する（未設定は許容）
   *
   * @param replicaUrl {@code app.datasource.replica.url}（環境変数 {@code APP_DATASOURCE_REPLICA_URL}）の値
   * @throws IllegalStateException 値が設定されているのに不正な場合
   */
  private void validateReplicaUrl(String replicaUrl) {
    if (!StringUtils.hasText(replicaUrl)) {
      return;
    }
    if (!replicaUrl.startsWith("jdbc:postgresql://")) {
      throw new IllegalStateException(
          "app.datasource.replica.url（環境変数 APP_DATASOURCE_REPLICA_URL）は jdbc:postgresql:// 形式である必要があります: "
              + replicaUrl);
    }
    if (replicaUrl.equals(dataSourceProperties.getUrl())) {
      throw new IllegalStateException(
          "app.datasource.replica.url が spring.datasource.url（DB_URL）と同一です。誤設定の疑いがあります: "
              + replicaUrl);
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
