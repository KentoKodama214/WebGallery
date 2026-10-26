package com.web.gallery.infrastructure.persistence.repository.photo;

import com.web.gallery.application.model.photo.FileModel;
import com.web.gallery.application.repository.photo.FileRepository;
import com.web.gallery.domain.model.photo.ImageFilePath;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
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
 * <p>画像の実体はS3に保存し、DBにはサーバ生成の不透明オブジェクトキー（{@code {アカウント番号}/{写真番号}-{ランダム}.{拡張子}}）のみを保持する。 閲覧時は {@link
 * #getPresignedUrl} で有効期限付きの署名付きURLを発行し、ブラウザがS3から直接取得する。
 */
@Repository
public class FileRepositoryImpl implements FileRepository {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final String bucket;
  private final int presignExpirySeconds;

  /**
   * コンストラクタ
   *
   * @param s3Client {@link S3Client}
   * @param s3Presigner {@link S3Presigner}
   * @param bucket バケット名
   * @param presignExpirySeconds 署名付きURLの有効期限（秒）
   */
  public FileRepositoryImpl(
      S3Client s3Client,
      S3Presigner s3Presigner,
      @Value("${app.s3.bucket}") String bucket,
      @Value("${app.s3.presign-expiry-seconds:900}") int presignExpirySeconds) {
    this.s3Client = s3Client;
    this.s3Presigner = s3Presigner;
    this.bucket = bucket;
    this.presignExpirySeconds = presignExpirySeconds;
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
              // Content-Type はクライアント申告値ではなく、検証済み拡張子から確定した値を用いる。
              // Content-Disposition: inline を明示し、配信時にダウンロード誘導・型の曖昧さを残さない
              .contentType(resolveContentType(key, multipartFile))
              .contentDisposition("inline")
              .build(),
          RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));
    } catch (IOException e) {
      throw new UncheckedIOException("画像ファイルの読み込みに失敗しました。(key: " + key + ")", e);
    }
  }

  /**
   * オブジェクトキーの拡張子から Content-Type を決定する
   *
   * <p>キーの拡張子はアップロード時に許可拡張子として検証済み。想定外の拡張子の場合のみ、フォールバックとして MultipartFile の申告値を用いる。
   *
   * @param key オブジェクトキー
   * @param multipartFile アップロードされたファイル
   * @return Content-Type
   */
  private static String resolveContentType(String key, MultipartFile multipartFile) {
    String lowerKey = key.toLowerCase(Locale.ROOT);
    if (lowerKey.endsWith(".jpg") || lowerKey.endsWith(".jpeg")) {
      return "image/jpeg";
    }
    if (lowerKey.endsWith(".png")) {
      return "image/png";
    }
    if (lowerKey.endsWith(".gif")) {
      return "image/gif";
    }
    if (lowerKey.endsWith(".webp")) {
      return "image/webp";
    }
    return multipartFile.getContentType();
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

  /**
   * {@inheritDoc}
   *
   * <p>ブラウザから到達可能なホストで署名する必要があるため、エンドポイントの切り替えは {@code S3Presigner}のBean定義（{@code
   * S3ClientConfig#s3Presigner}）側で行う。発行済みURLの ホストを後から差し替えるとSigV4の署名（{@code
   * host}を署名対象に含む）が壊れるため、ここでは加工しない。
   */
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
    return new ImageFilePath(presignedUrl);
  }
}
