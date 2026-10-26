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
 * <p><b>{@code RemoteIpValve}と異なる点</b>：各要素をIPアドレスとして検証・正規化してから扱う。 {@code
 * RemoteIpValve}は要素を素通しするが、前段が{@code X-Forwarded-For}を追記せず素通しする構成では
 * 右端＝クライアントの指定値になり、任意の文字列がそのまま{@code getRemoteAddr()}になってしまう。 復元したIPは{@code
 * common.login_history.ip_address} / {@code photo.photo_view_log.ip_address} （いずれも{@code
 * varchar(45)}）へ保存されるため、不正な値や長すぎる値はINSERT失敗を招き、GeoIP解決にも
 * 渡ってしまう。IPアドレスとして読めない要素が現れた時点で差し替えを諦め、TCP接続の送信元IPに倒す。
 *
 * <p>あわせて{@code 1.2.3.4:5678} / {@code [2001:db8::1]:443}のようなポート付き表記からポートを剥がす （Azure Front
 * Door等の前段が付ける）。剥がした結果で信頼プロキシ判定を行うため、ポート付きで届いた 前段プロキシのアドレスも正しく読み飛ばせる。
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
   * 採用するIPアドレスの最大文字数
   *
   * <p>{@code common.login_history.ip_address} / {@code photo.photo_view_log.ip_address} の {@code
   * varchar(45)}に合わせる。正規表記のIPv6は最長45文字（{@code xxxx:}×6＋{@code 255.255.255.255}）のため、
   * 妥当なIPアドレスがこの上限に掛かることはない
   */
  private static final int MAX_IP_LENGTH = 45;

  /** IPv4アドレスの表記に一致する正規表現（各オクテットは0-255、先行ゼロは許さない） */
  private static final Pattern IPV4_PATTERN =
      Pattern.compile(
          "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)){3}");

  /** IPv6の1グループ（16bit）の表記に一致する正規表現 */
  private static final Pattern IPV6_GROUP_PATTERN = Pattern.compile("[0-9A-Fa-f]{1,4}");

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
      String entry = normalizeIp(forwardedFor.get(i));
      // IPアドレスとして読めない要素が現れたらチェーンが壊れているとみなし、差し替えない。
      // 不正な値をそのまま採用すると、ログテーブルのvarchar(45)へのINSERT失敗やGeoIPの誤解決を招く
      if (entry == null) {
        return null;
      }
      if (!trustedProxyPattern.matcher(entry).matches()) {
        return entry;
      }
    }
    // 全要素が信頼できるプロキシだった場合は差し替えない（接続元IPをそのまま使う）
    return null;
  }

  /**
   * {@code X-Forwarded-For}の1要素を、ポート表記を取り除いたIPアドレスへ正規化する
   *
   * <p>名前解決は一切行わない（{@code InetAddress#getByName}は不正な値でDNS参照へ抜けるため使わない）
   *
   * @param entry {@code X-Forwarded-For}の1要素（trim済み）
   * @return 正規化したIPアドレス。IPアドレスとして読めない場合はnull
   */
  private static String normalizeIp(String entry) {
    String value = stripPort(entry);
    if (value == null || value.isEmpty() || value.length() > MAX_IP_LENGTH) {
      return null;
    }
    return isIpv4Address(value) || isIpv6Address(value) ? value : null;
  }

  /**
   * ポート表記を取り除く
   *
   * <p>{@code [2001:db8::1]:443} / {@code [2001:db8::1]} は括弧内を、{@code 1.2.3.4:5678} はコロンより前を返す。
   * コロンを複数含む括弧なしの値はIPv6そのものとみなし、何も取り除かない
   *
   * @param entry {@code X-Forwarded-For}の1要素（trim済み）
   * @return ポートを取り除いた文字列。表記として壊れている場合はnull
   */
  private static String stripPort(String entry) {
    if (entry.startsWith("[")) {
      int end = entry.indexOf(']');
      // 括弧が閉じていない、または括弧の後がポート表記でない場合は壊れた値として扱う
      if (end < 0 || (end != entry.length() - 1 && entry.charAt(end + 1) != ':')) {
        return null;
      }
      return entry.substring(1, end);
    }
    int colon = entry.indexOf(':');
    // コロンが1つだけならIPv4のポート付き表記（IPv6は必ず2つ以上のコロンを含む）
    return colon >= 0 && colon == entry.lastIndexOf(':') ? entry.substring(0, colon) : entry;
  }

  /**
   * IPv4アドレスの表記として妥当かどうかを判定する
   *
   * @param value 判定対象の文字列
   * @return 妥当な場合、true
   */
  private static boolean isIpv4Address(String value) {
    return IPV4_PATTERN.matcher(value).matches();
  }

  /**
   * IPv6アドレスの表記として妥当かどうかを判定する
   *
   * <p>ゼロ圧縮（{@code ::}）は1箇所まで、末尾のIPv4表記（IPv4射影アドレス）は2グループ分として数え、
   * 圧縮なしでちょうど8グループ・圧縮ありで8グループ未満であることを確認する。ゾーンID付き （{@code
   * fe80::1%eth0}）はリンクローカル専用でクライアントIPになり得ないため受け付けない
   *
   * @param value 判定対象の文字列
   * @return 妥当な場合、true
   */
  private static boolean isIpv6Address(String value) {
    if (value.indexOf(':') < 0) {
      return false;
    }
    int compressed = value.indexOf("::");
    // ゼロ圧縮は1箇所までしか使えない（2箇所あると省略されたグループ数が一意に定まらない）
    if (compressed >= 0 && value.indexOf("::", compressed + 2) >= 0) {
      return false;
    }

    String head = compressed < 0 ? value : value.substring(0, compressed);
    String tail = compressed < 0 ? "" : value.substring(compressed + 2);
    // 末尾のIPv4表記はアドレス全体の末尾にしか現れない
    int headGroups = countIpv6Groups(head, compressed < 0);
    int tailGroups = countIpv6Groups(tail, compressed >= 0);
    if (headGroups < 0 || tailGroups < 0) {
      return false;
    }

    int total = headGroups + tailGroups;
    return compressed < 0 ? total == 8 : total < 8;
  }

  /**
   * ゼロ圧縮で区切られたIPv6アドレスの片側について、16bitグループ数を数える
   *
   * @param part ゼロ圧縮の前半または後半（空文字可）
   * @param allowIpv4Suffix 末尾のIPv4表記を許すかどうか
   * @return グループ数。表記が不正な場合は-1
   */
  private static int countIpv6Groups(String part, boolean allowIpv4Suffix) {
    if (part.isEmpty()) {
      return 0;
    }
    // ゼロ圧縮以外でコロンが連続する・端にコロンが来る表記は不正
    if (part.startsWith(":") || part.endsWith(":")) {
      return -1;
    }

    String[] groups = part.split(":", -1);
    int count = 0;
    for (int i = 0; i < groups.length; i++) {
      String group = groups[i];
      if (allowIpv4Suffix && i == groups.length - 1 && group.indexOf('.') >= 0) {
        if (!isIpv4Address(group)) {
          return -1;
        }
        // IPv4表記は32bit＝16bitグループ2つ分
        count += 2;
        continue;
      }
      if (!IPV6_GROUP_PATTERN.matcher(group).matches()) {
        return -1;
      }
      count++;
    }
    return count;
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
