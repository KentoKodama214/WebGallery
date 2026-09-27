package com.web.gallery.infrastructure.config;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * 画像ストレージ（S3 / MinIO）クライアントのBean定義クラス
 *
 * <p>{@link S3Config} のプロパティから {@link S3Client}（オブジェクト操作用）と {@link S3Presigner}（署名付きURL発行用）を構築する。
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class S3ClientConfig {

  private final S3Config s3Config;

  /**
   * S3オブジェクト操作用のクライアントを生成する
   *
   * @return {@link S3Client}
   */
  @Bean
  public S3Client s3Client() {
    var builder =
        S3Client.builder()
            .region(Region.of(s3Config.getRegion()))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(s3Config.getAccessKey(), s3Config.getSecretKey())))
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(Boolean.TRUE.equals(s3Config.getPathStyleAccess()))
                    .build());
    if (s3Config.getEndpoint() != null && !s3Config.getEndpoint().isBlank()) {
      builder.endpointOverride(URI.create(s3Config.getEndpoint()));
    }
    return builder.build();
  }

  /**
   * 署名付きURL発行用のプリサイナーを生成する
   *
   * <p>SigV4の署名対象には{@code host}ヘッダーが必ず含まれるため、発行後のURLでホストを差し替えると
   * 署名検証に失敗する。ブラウザから到達可能なホストで署名させる必要があるので、{@code app.s3.public-base-url}
   * が設定されている場合はそちらをエンドポイントとして用いる（未設定なら{@code app.s3.endpoint}にフォールバック）。 アップロード等のサーバー間通信を担う{@link
   * #s3Client()}は、従来どおり内部向けの{@code app.s3.endpoint}を使う。
   *
   * @return {@link S3Presigner}
   */
  @Bean
  public S3Presigner s3Presigner() {
    var builder =
        S3Presigner.builder()
            .region(Region.of(s3Config.getRegion()))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(s3Config.getAccessKey(), s3Config.getSecretKey())))
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(Boolean.TRUE.equals(s3Config.getPathStyleAccess()))
                    .build());
    String presignEndpoint = resolvePresignEndpoint();
    if (presignEndpoint != null) {
      builder.endpointOverride(URI.create(presignEndpoint));
    }
    return builder.build();
  }

  /**
   * 署名付きURLの発行に用いるエンドポイントを決定する
   *
   * @return 公開ベースURL、未設定ならS3互換エンドポイント。どちらも未設定ならnull（AWS S3の既定エンドポイント）
   */
  private String resolvePresignEndpoint() {
    String publicBaseUrl = s3Config.getPublicBaseUrl();
    if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
      return publicBaseUrl;
    }
    String endpoint = s3Config.getEndpoint();
    return endpoint != null && !endpoint.isBlank() ? endpoint : null;
  }
}
