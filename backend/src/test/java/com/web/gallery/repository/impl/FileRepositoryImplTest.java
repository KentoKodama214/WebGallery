package com.web.gallery.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.web.gallery.domain.photo.ImageFile;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.model.FileModel;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

/** {@link FileRepositoryImpl} の単体テスト */
@ExtendWith(MockitoExtension.class)
class FileRepositoryImplTest {

  private static final String BUCKET = "test-bucket";
  private static final int EXPIRY_SECONDS = 900;

  @Mock private S3Client s3Client;

  @Mock private S3Presigner s3Presigner;

  private FileRepositoryImpl newRepository(String publicBaseUrl) {
    return new FileRepositoryImpl(s3Client, s3Presigner, BUCKET, EXPIRY_SECONDS, publicBaseUrl);
  }

  @Nested
  @DisplayName("save")
  class Save {
    @Test
    @DisplayName("正常系：バケット・キー・Content-Typeを指定してputObjectを呼び出す")
    void save_putsObject() throws IOException {
      MultipartFile multipartFile = mock(MultipartFile.class);
      doReturn("image/jpeg").when(multipartFile).getContentType();
      doReturn(6L).when(multipartFile).getSize();
      doReturn(new java.io.ByteArrayInputStream(new byte[] {1, 2, 3, 4, 5, 6}))
          .when(multipartFile)
          .getInputStream();

      FileModel fileModel =
          FileModel.of(new ImageFilePath("aaaaaaaa/DSC11.jpg"), new ImageFile(multipartFile));

      newRepository("").save(fileModel);

      ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
      verify(s3Client)
          .putObject(captor.capture(), any(software.amazon.awssdk.core.sync.RequestBody.class));
      assertEquals(BUCKET, captor.getValue().bucket());
      assertEquals("aaaaaaaa/DSC11.jpg", captor.getValue().key());
      assertEquals("image/jpeg", captor.getValue().contentType());
    }

    @Test
    @DisplayName("異常系：InputStream取得に失敗した場合はUncheckedIOExceptionをthrowする")
    void save_ioException_wrapped() throws IOException {
      MultipartFile multipartFile = mock(MultipartFile.class);
      doThrow(new IOException("読み込み失敗")).when(multipartFile).getInputStream();

      FileModel fileModel =
          FileModel.of(new ImageFilePath("aaaaaaaa/DSC11.jpg"), new ImageFile(multipartFile));

      FileRepositoryImpl repository = newRepository("");
      assertThrows(UncheckedIOException.class, () -> repository.save(fileModel));
      verify(s3Client, never())
          .putObject(
              any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class));
    }
  }

  @Nested
  @DisplayName("delete")
  class Delete {
    @Test
    @DisplayName("正常系：キーを指定してdeleteObjectを呼び出す")
    void delete_deletesObject() {
      newRepository("").delete(new ImageFilePath("aaaaaaaa/DSC11.jpg"));

      ArgumentCaptor<DeleteObjectRequest> captor =
          ArgumentCaptor.forClass(DeleteObjectRequest.class);
      verify(s3Client).deleteObject(captor.capture());
      assertEquals(BUCKET, captor.getValue().bucket());
      assertEquals("aaaaaaaa/DSC11.jpg", captor.getValue().key());
    }

    @Test
    @DisplayName("正常系：キーが'/'終端の場合はプレフィックス一括削除に委譲する")
    void delete_trailingSlash_delegatesToPrefixDeletion() {
      doReturn(ListObjectsV2Response.builder().contents(List.of()).isTruncated(false).build())
          .when(s3Client)
          .listObjectsV2(any(ListObjectsV2Request.class));

      newRepository("").delete(new ImageFilePath("aaaaaaaa/"));

      verify(s3Client).listObjectsV2(any(ListObjectsV2Request.class));
      verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }
  }

  @Nested
  @DisplayName("deleteByPrefix")
  class DeleteByPrefix {
    @Test
    @DisplayName("正常系：プレフィックス配下のオブジェクトをまとめて削除する")
    void deleteByPrefix_deletesAllListedObjects() {
      doReturn(
              ListObjectsV2Response.builder()
                  .contents(
                      S3Object.builder().key("aaaaaaaa/DSC11.jpg").build(),
                      S3Object.builder().key("aaaaaaaa/DSC12.jpg").build())
                  .isTruncated(false)
                  .build())
          .when(s3Client)
          .listObjectsV2(any(ListObjectsV2Request.class));

      newRepository("").deleteByPrefix(new ImageFilePath("aaaaaaaa/"));

      ArgumentCaptor<DeleteObjectsRequest> captor =
          ArgumentCaptor.forClass(DeleteObjectsRequest.class);
      verify(s3Client).deleteObjects(captor.capture());
      List<String> deletedKeys =
          captor.getValue().delete().objects().stream()
              .map(software.amazon.awssdk.services.s3.model.ObjectIdentifier::key)
              .toList();
      assertEquals(List.of("aaaaaaaa/DSC11.jpg", "aaaaaaaa/DSC12.jpg"), deletedKeys);
    }

    @Test
    @DisplayName("正常系：対象が0件の場合はdeleteObjectsを呼び出さない")
    void deleteByPrefix_noObjects_doesNotDelete() {
      doReturn(ListObjectsV2Response.builder().contents(List.of()).isTruncated(false).build())
          .when(s3Client)
          .listObjectsV2(any(ListObjectsV2Request.class));

      newRepository("").deleteByPrefix(new ImageFilePath("aaaaaaaa/"));

      verify(s3Client, never()).deleteObjects(any(DeleteObjectsRequest.class));
    }
  }

  @Nested
  @DisplayName("getPresignedUrl")
  class GetPresignedUrl {
    private void stubPresign(String url) {
      PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
      doReturn(toUrl(url)).when(presigned).url();
      doReturn(presigned).when(s3Presigner).presignGetObject(any(GetObjectPresignRequest.class));
    }

    private java.net.URL toUrl(String url) {
      try {
        return URI.create(url).toURL();
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
    }

    @Test
    @DisplayName("正常系：public-base-url未設定の場合は発行された署名付きURLをそのまま返す")
    void getPresignedUrl_noPublicBaseUrl_returnsAsIs() {
      stubPresign("http://s3.internal:9000/test-bucket/aaaaaaaa/DSC11.jpg?X-Amz-Signature=abc123");

      ImageFilePath actual =
          newRepository("").getPresignedUrl(new ImageFilePath("aaaaaaaa/DSC11.jpg"));

      assertEquals(
          "http://s3.internal:9000/test-bucket/aaaaaaaa/DSC11.jpg?X-Amz-Signature=abc123",
          actual.value());
    }

    @Test
    @DisplayName("正常系：public-base-url設定時はスキーム・ホスト・ポートを差し替え、パスとクエリは維持する")
    void getPresignedUrl_withPublicBaseUrl_rewritesHost() {
      stubPresign("http://s3.internal:9000/test-bucket/aaaaaaaa/DSC11.jpg?X-Amz-Signature=abc123");

      ImageFilePath actual =
          newRepository("http://localhost:9000")
              .getPresignedUrl(new ImageFilePath("aaaaaaaa/DSC11.jpg"));

      assertEquals(
          "http://localhost:9000/test-bucket/aaaaaaaa/DSC11.jpg?X-Amz-Signature=abc123",
          actual.value());
    }
  }
}
