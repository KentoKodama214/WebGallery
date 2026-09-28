package com.web.gallery.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 信頼できるプロキシ経由の{@code X-Forwarded-For}から実クライアントIPを復元するフィルター
 *
 * <p>TCP接続の送信元（{@code getRemoteAddr()}）が{@code app.forwarded.trusted-proxies}（環境変数 {@code
 * TRUSTED_PROXIES}）に一致する場合にのみ{@code X-Forwarded-For}を採用し、{@link HttpServletRequestWrapper}で {@code
 * getRemoteAddr()} / {@code getRemoteHost()} を実クライアントIPへ差し替える。範囲外から届いた {@code
 * X-Forwarded-For}は無視するため、クライアントによる送信元IPの詐称（レート制限の回避・ログイン履歴や アクセスログ・GeoIPの偽装）が成立しない。
 *
 * <p><b>なぜTomcatの{@code RemoteIpValve}に委ねないか</b>：本アプリはWARを外部Tomcatへデプロイする構成 （{@code
 * ServletInitializer} / {@code build.gradle}の{@code war}プラグイン）であり、Spring Bootの {@code
 * server.tomcat.remoteip.*} / {@code server.forward-headers-strategy} は**埋め込みサーバー （{@code
 * ConfigurableTomcatWebServerFactory}）向けの設定のため本番では読まれない**。設定した気になって
 * 保護が効かない状態を避けるため、コンテナ構成に依存しないアプリ側のフィルターとして実装する。 これによりWAR・{@code bootRun}・テストで同一の挙動になる。
 *
 * <p>復元アルゴリズムは{@code RemoteIpValve}と同等で、{@code X-Forwarded-For}を右端から左へ辿り、
 * 信頼できるプロキシに一致する要素を読み飛ばして最初に現れた要素を実クライアントIPとする （前段が追記する仕様のため、右側が信頼境界に近い）。全要素が信頼できるプロキシだった場合は差し替えない。
 *
 * <p>{@code X-Forwarded-Proto}は扱わない。リフレッシュトークンCookieの{@code Secure}属性は
 * リクエストのスキームに依らず常に付与しており（{@code AuthController}）、{@code request.isSecure()} / {@code
 * getScheme()}に依存する処理が存在しないため。将来依存が生まれた場合はここで併せて復元する。
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ForwardedForFilter extends OncePerRequestFilter {

  /** 前段プロキシが実クライアントIPを載せるヘッダー名 */
  private static final String X_FORWARDED_FOR = "X-Forwarded-For";

  /**
   * 信頼する送信元IPに一致する正規表現（未設定・不正な場合はnull＝どの送信元も信頼しない）
   *
   * <p>リクエストごとのコンパイルを避けるため、コンストラクタで1度だけ取得して保持する
   */
  private final Pattern trustedProxyPattern;

  /**
   * コンストラクタ
   *
   * @param trustedProxyConfig {@code X-Forwarded-For}を信頼する送信元の設定
   */
  public ForwardedForFilter(TrustedProxyConfig trustedProxyConfig) {
    this.trustedProxyPattern = trustedProxyConfig.trustedProxyPattern();
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String clientIp = resolveClientIp(request);
    filterChain.doFilter(clientIp == null ? request : wrap(request, clientIp), response);
  }

  /**
   * 信頼できるプロキシ経由の{@code X-Forwarded-For}から実クライアントIPを求める
   *
   * @param request リクエスト
   * @return 差し替えるべき実クライアントIP。差し替え不要な場合はnull
   */
  private String resolveClientIp(HttpServletRequest request) {
    if (trustedProxyPattern == null) {
      return null;
    }
    String remoteAddr = request.getRemoteAddr();
    // TCP接続の送信元が信頼できるプロキシでなければ、X-Forwarded-For は一切信用しない
    if (!StringUtils.hasText(remoteAddr) || !trustedProxyPattern.matcher(remoteAddr).matches()) {
      return null;
    }

    List<String> forwardedFor = forwardedForEntries(request);
    // 前段は X-Forwarded-For を上書きせず追記するため、信頼境界に近い右端から左へ辿り、
    // 信頼できるプロキシを読み飛ばして最初に現れた要素を実クライアントIPとする
    for (int i = forwardedFor.size() - 1; i >= 0; i--) {
      String entry = forwardedFor.get(i);
      if (!trustedProxyPattern.matcher(entry).matches()) {
        return entry;
      }
    }
    // 全要素が信頼できるプロキシだった場合は差し替えない（接続元IPをそのまま使う）
    return null;
  }

  /**
   * {@code X-Forwarded-For}ヘッダーの値を左から右の順で1件ずつのリストにする
   *
   * <p>ヘッダーが複数行に分かれて届く場合も、受信順に連結して1つのチェーンとして扱う
   *
   * @param request リクエスト
   * @return 空要素を除いたチェーン。ヘッダーが無い場合は空リスト
   */
  private static List<String> forwardedForEntries(HttpServletRequest request) {
    List<String> entries = new ArrayList<>();
    for (String headerValue : Collections.list(request.getHeaders(X_FORWARDED_FOR))) {
      if (!StringUtils.hasText(headerValue)) {
        continue;
      }
      for (String entry : headerValue.split(",")) {
        String trimmed = entry.trim();
        if (!trimmed.isEmpty()) {
          entries.add(trimmed);
        }
      }
    }
    return entries;
  }

  /**
   * 送信元IPを差し替えたリクエストを生成する
   *
   * @param request 元のリクエスト
   * @param clientIp 実クライアントIP
   * @return {@code getRemoteAddr()} / {@code getRemoteHost()} が実クライアントIPを返すリクエスト
   */
  private static HttpServletRequest wrap(HttpServletRequest request, String clientIp) {
    return new HttpServletRequestWrapper(request) {
      @Override
      public String getRemoteAddr() {
        return clientIp;
      }

      @Override
      public String getRemoteHost() {
        // 逆引きは行わない（RemoteIpValveと同様にIPをそのまま返す）
        return clientIp;
      }
    };
  }
}
