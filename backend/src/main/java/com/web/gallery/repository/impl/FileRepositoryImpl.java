package com.web.gallery.repository.impl;

import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.model.FileModel;
import com.web.gallery.repository.FileRepository;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

/**
 * ファイルをS3（互換）ストレージへ永続化するRepositoryの実装クラス
 *
 * <p>画像の実体はS3に保存し、DBにはオブジェクトキー（{@code {accountId}/{ファイル名}}）のみを保持する。 閲覧時は {@link #getPresignedUrl}
 * で有効期限付きの署名付きURLを発行し、ブラウザがS3から直接取得する。
 */
@Slf4j
@Repository
public class FileRepositoryImpl implements FileRepository {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final String bucket;
  private final int presignExpirySeconds;
  private final String publicBaseUrl;

  /**
   * コンストラクタ
   *
   * @param s3Client {@link S3Client}
   * @param s3Presigner {@link S3Presigner}
   * @param bucket バケット名
   * @param presignExpirySeconds 署名付きURLの有効期限（秒）
   * @param publicBaseUrl ブラウザから到達可能な公開ベースURL（未設定なら空文字）
   */
  public FileRepositoryImpl(
      S3Client s3Client,
      S3Presigner s3Presigner,
      @Value("${app.s3.bucket}") String bucket,
      @Value("${app.s3.presign-expiry-seconds:900}") int presignExpirySeconds,
      @Value("${app.s3.public-base-url:}") String publicBaseUrl) {
    this.s3Client = s3Client;
    this.s3Presigner = s3Presigner;
    this.bucket = bucket;
    this.presignExpirySeconds = presignExpirySeconds;
    this.publicBaseUrl = publicBaseUrl;
  }

  @Override
  public void save(FileModel fileModel) {
    String key = fileModel.getFilePath().value();
    MultipartFile multipartFile = fileModel.getImageFile().value();
    try {
      s3Client.putObject(
          PutObjectRequest.builder()
              .bucket(bucket)
              .key(key)
              .contentType(multipartFile.getContentType())
              .build(),
          RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));
    } catch (IOException e) {
      throw new UncheckedIOException("画像ファイルの読み込みに失敗しました。(key: " + key + ")", e);
    }
  }

  @Override
  public void delete(ImageFilePath filePath) {
    String key = filePath.value();
    // キーが "/" 終端の場合はディレクトリ相当とみなし、配下を一括削除する
    if (key.endsWith("/")) {
      deleteByPrefix(filePath);
      return;
    }
    s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
  }

  @Override
  public void deleteByPrefix(ImageFilePath prefix) {
    String keyPrefix = prefix.value();
    String continuationToken = null;
    do {
      ListObjectsV2Response listResponse =
          s3Client.listObjectsV2(
              ListObjectsV2Request.builder()
                  .bucket(bucket)
                  .prefix(keyPrefix)
                  .continuationToken(continuationToken)
                  .build());

      List<ObjectIdentifier> objectIds =
          listResponse.contents().stream()
              .map(object -> ObjectIdentifier.builder().key(object.key()).build())
              .toList();

      if (!objectIds.isEmpty()) {
        s3Client.deleteObjects(
            DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(Delete.builder().objects(objectIds).build())
                .build());
      }

      continuationToken =
          Boolean.TRUE.equals(listResponse.isTruncated())
              ? listResponse.nextContinuationToken()
              : null;
    } while (continuationToken != null);
  }

  @Override
  public ImageFilePath getPresignedUrl(ImageFilePath filePath) {
    String key = filePath.value();
    String presignedUrl =
        s3Presigner
            .presignGetObject(
                GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(presignExpirySeconds))
                    .getObjectRequest(builder -> builder.bucket(bucket).key(key))
                    .build())
            .url()
            .toString();
    return new ImageFilePath(rewriteToPublicBaseUrl(presignedUrl));
  }

  /**
   * 署名付きURLのスキーム・ホスト・ポートを公開ベースURLのものへ差し替える
   *
   * <p>S3エンドポイントがコンテナ内部のホスト名等でブラウザから到達できない場合に用いる。 {@code publicBaseUrl} が未設定なら差し替えず、そのまま返す。
   *
   * @param presignedUrl 発行された署名付きURL
   * @return 差し替え後のURL文字列
   */
  private String rewriteToPublicBaseUrl(String presignedUrl) {
    if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
      return presignedUrl;
    }
    try {
      URI original = URI.create(presignedUrl);
      URI base = URI.create(publicBaseUrl);
      String hostPort = base.getHost() + (base.getPort() == -1 ? "" : ":" + base.getPort());
      // 署名済みのパス・クエリは再エンコードせずそのまま連結する（%の二重エンコードで署名を壊さないため）
      return base.getScheme()
          + "://"
          + hostPort
          + original.getRawPath()
          + (original.getRawQuery() == null ? "" : "?" + original.getRawQuery());
    } catch (IllegalArgumentException e) {
      log.warn("署名付きURLの公開ベースURLへの差し替えに失敗しました。発行値をそのまま返します。", e);
      return presignedUrl;
    }
  }
}
