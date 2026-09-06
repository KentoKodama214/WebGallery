package com.web.gallery.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.ymlの画像ストレージ（S3 / MinIO）に関するプロパティを保持するConfigクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@RequiredArgsConstructor
@Getter
@ConfigurationProperties(prefix = "app.s3")
public class S3Config {
  /**
   * S3互換エンドポイント
   *
   * <p>MinIO等のS3互換ストレージを利用する場合に指定する。未指定（null）の場合はAWS S3の 既定エンドポイントを利用する。
   */
  private final String endpoint;

  /** リージョン */
  private final String region;

  /** バケット名 */
  private final String bucket;

  /** アクセスキー */
  private final String accessKey;

  /** シークレットキー */
  private final String secretKey;

  /**
   * パススタイルアクセスを利用するかどうか
   *
   * <p>MinIO等、仮想ホスト形式のバケットURLを解決できない環境ではtrueにする。
   */
  private final Boolean pathStyleAccess;

  /** 署名付きURLの有効期限（秒） */
  private final Integer presignExpirySeconds;

  /**
   * ブラウザから到達可能な公開ベースURL
   *
   * <p>{@link #endpoint} がコンテナ内部のホスト名等でブラウザから到達できない場合に、
   * 発行した署名付きURLのスキーム・ホスト・ポートをこの値へ差し替える。未指定（null）なら差し替えない。
   */
  private final String publicBaseUrl;
}
