package com.web.gallery.infrastructure.config;

import com.web.gallery.application.config.WebAuthnConfig;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * application.ymlのパスキー（WebAuthn）に関するプロパティを保持するConfigクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@RequiredArgsConstructor
@Getter
@ConfigurationProperties(prefix = "app.webauthn")
public class WebAuthnConfigImpl implements WebAuthnConfig {
  /** RP ID（パスキーを紐づけるドメイン。スキーム・ポートを含めない） */
  private final String rpId;

  /** RP名（パスキーマネージャーに表示されるサービス名） */
  private final String rpName;

  /** パスキーの登録・認証を許可するorigin（フロントエンドのオリジン） */
  private final List<String> allowedOrigins;

  /** チャレンジの有効期間（秒） */
  private final Integer challengeTimeoutSeconds;

  /**
   * 起動時にRP ID・許可するoriginの整合性を検証する
   *
   * <p>WebAuthnでは、originのホストがRP IDと一致するか、RP IDのサブドメインである必要がある。
   * 設定が食い違っているとパスキーの登録・認証がブラウザ側ですべて失敗するため、誤設定を起動時に検出する （本番プロファイルでの{@code https://}限定・{@code
   * localhost}禁止のチェックはProdConfigValidationRunnerが担う）。
   *
   * @throws IllegalStateException 未設定、またはoriginのホストがRP IDと整合しない場合
   */
  @PostConstruct
  void validate() {
    if (!StringUtils.hasText(rpId) || rpId.matches(".*[\\s/:].*")) {
      throw new IllegalStateException(
          "app.webauthn.rp-id（環境変数 WEBAUTHN_RP_ID）にスキーム・ポート・パスを含まないドメインを設定する必要があります: ["
              + rpId
              + "]");
    }
    if (!StringUtils.hasText(rpName)) {
      throw new IllegalStateException("app.webauthn.rp-name を設定する必要があります");
    }
    if (challengeTimeoutSeconds == null || challengeTimeoutSeconds <= 0) {
      throw new IllegalStateException("app.webauthn.challenge-timeout-seconds は1以上を設定する必要があります");
    }
    if (CollectionUtils.isEmpty(allowedOrigins)
        || allowedOrigins.stream().noneMatch(StringUtils::hasText)) {
      throw new IllegalStateException(
          "app.webauthn.allowed-origins（環境変数 FRONTEND_ORIGIN）を1件以上設定する必要があります");
    }
    for (String origin : allowedOrigins) {
      String host = hostOf(origin);
      if (!host.equals(rpId) && !host.endsWith("." + rpId)) {
        throw new IllegalStateException(
            "app.webauthn.allowed-origins のホストは RP ID（"
                + rpId
                + "）と一致するかそのサブドメインである必要があります: ["
                + origin
                + "]");
      }
    }
  }

  /**
   * originからホスト名を取り出す
   *
   * @param origin origin
   * @return ホスト名
   * @throws IllegalStateException originとして解釈できない場合
   */
  private String hostOf(String origin) {
    try {
      URI uri = new URI(origin);
      if (uri.getScheme() == null
          || uri.getHost() == null
          || (uri.getPath() != null && !uri.getPath().isEmpty())) {
        throw new IllegalStateException(
            "app.webauthn.allowed-origins の各値はパスを含まない単一のオリジンである必要があります: [" + origin + "]");
      }
      return uri.getHost();
    } catch (URISyntaxException e) {
      throw new IllegalStateException(
          "app.webauthn.allowed-origins の値がオリジンとして不正です: [" + origin + "]", e);
    }
  }
}
