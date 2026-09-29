package com.web.gallery.infrastructure.web;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * application.ymlの「{@code X-Forwarded-For}を信頼する送信元」に関するプロパティを保持するConfigクラス
 *
 * <p>以前はTomcatの{@code RemoteIpValve}（{@code server.tomcat.remoteip.*}）に委ねていたが、
 * 本アプリはWARを外部Tomcatへデプロイする構成のため、Spring Bootの{@code server.tomcat.*}は
 * 埋め込みサーバー向けの設定であり**本番では読まれない**。信頼境界の判定をコンテナ構成に依存させず、 WAR / {@code bootRun} / テストで同一の挙動にするため、{@link
 * ForwardedForFilter}がこの設定を用いて アプリ側で解決する。
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@RequiredArgsConstructor
@Getter
@ConfigurationProperties(prefix = "app.forwarded")
public class TrustedProxyConfig {

  /**
   * {@code X-Forwarded-For}を信頼する「直前の送信元IP」に一致する正規表現
   *
   * <p>この範囲外から届いた{@code X-Forwarded-For}は無視し、TCP接続の実IPを送信元とみなす
   * （クライアントによるIP詐称の防止）。本番では前段のALB／リバースプロキシが存在するサブネットの CIDRだけに狭めること（環境変数{@code TRUSTED_PROXIES}）。
   */
  private final String trustedProxies;

  /**
   * 信頼する送信元の正規表現をコンパイル済みの{@link Pattern}として返す
   *
   * <p>未設定・正規表現として不正な場合は「どの送信元も信頼しない」を表す{@code null}を返す。 設定値の妥当性検証は{@code
   * ProdConfigValidationRunner}（prodプロファイル）が起動時に行う。
   *
   * <p>呼び出しごとにコンパイルするため、リクエストごとに呼ばず呼び出し側で保持すること （{@link ForwardedForFilter}はコンストラクタで1度だけ取得する）。
   *
   * @return コンパイル済みの正規表現。未設定・不正な場合はnull
   */
  public Pattern trustedProxyPattern() {
    if (!StringUtils.hasText(trustedProxies)) {
      return null;
    }
    try {
      return Pattern.compile(trustedProxies.trim());
    } catch (PatternSyntaxException e) {
      return null;
    }
  }
}
