package com.web.gallery.repository.impl.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.photo.ImageFile;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.model.photo.FileModel;
import com.web.gallery.repository.FileRepository;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * {@link com.web.gallery.repository.impl.FileRepositoryImpl} の統合テスト
 *
 * <p>Controller/Service層の統合テストでは{@link FileRepository}をモック化するため、実際のS3（互換）ストレージへの
 * put/get/delete/presignはこのテストでのみ検証する。docker-compose（ローカル）またはCI（{@code
 * .github/workflows/test.yml}のminioサービス）が提供するMinIOへ実接続する
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class FileRepositoryImplIntegrationTest {
  @Autowired private FileRepository fileRepository;

  @Autowired private S3Client s3Client;

  @Value("${app.s3.bucket}")
  private String bucket;

  private static final byte[] JPEG_BYTES = new byte[] {1, 2, 3, 4, 5, 6, 7, 8};

  /** テストで作成したオブジェクトキーのうち、テスト内でdeleteされなかったものを後始末する */
  private void deleteQuietly(String key) {
    try {
      s3Client.deleteObject(builder -> builder.bucket(bucket).key(key));
    } catch (RuntimeException ignored) {
      // 既に削除済みの場合は無視する
    }
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class save {
    private static final String KEY = "integration-test/save-target.jpg";

    @AfterEach
    void tearDown() {
      s3Client.deleteObject(builder -> builder.bucket(bucket).key(KEY));
    }

    @Test
    @Order(1)
    @DisplayName("正常系：実際にputObjectされ、取得したファイルの内容・Content-Typeが一致すること")
    void save_putsRetrievableObject() throws IOException {
      MockMultipartFile multipartFile =
          new MockMultipartFile("imageFiles", "original.jpg", "image/png", JPEG_BYTES);
      FileModel fileModel = FileModel.of(new ImageFilePath(KEY), new ImageFile(multipartFile));

      fileRepository.save(fileModel);

      ResponseInputStream<GetObjectResponse> response =
          s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key(KEY).build());
      byte[] actualBytes = response.readAllBytes();
      assertArrayEquals(JPEG_BYTES, actualBytes);
      // クライアント申告値（image/png）ではなく、キーの拡張子（.jpg）から確定した値であること
      assertEquals("image/jpeg", response.response().contentType());
      assertEquals("inline", response.response().contentDisposition());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class delete {
    private static final String KEY = "integration-test/delete-target.jpg";

    @Test
    @Order(1)
    @DisplayName("正常系：指定したキーのオブジェクトが削除され、以後取得できなくなること")
    void delete_removesObject() {
      s3Client.putObject(
          PutObjectRequest.builder().bucket(bucket).key(KEY).build(),
          software.amazon.awssdk.core.sync.RequestBody.fromBytes(JPEG_BYTES));

      fileRepository.delete(new ImageFilePath(KEY));

      assertThrows(
          NoSuchKeyException.class,
          () -> s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(KEY).build()));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class deleteByPrefix {
    private static final String PREFIX = "integration-test/delete-by-prefix/";

    @Test
    @Order(1)
    @DisplayName("正常系：プレフィックス配下の全オブジェクトが削除され、配下以外のオブジェクトは残ること")
    void deleteByPrefix_removesAllObjectsUnderPrefixOnly() {
      String keyUnderPrefix1 = PREFIX + "1.jpg";
      String keyUnderPrefix2 = PREFIX + "2.jpg";
      String keyOutsidePrefix = "integration-test/delete-by-prefix-sibling/3.jpg";
      for (String key : List.of(keyUnderPrefix1, keyUnderPrefix2, keyOutsidePrefix)) {
        s3Client.putObject(
            PutObjectRequest.builder().bucket(bucket).key(key).build(),
            software.amazon.awssdk.core.sync.RequestBody.fromBytes(JPEG_BYTES));
      }

      try {
        fileRepository.deleteByPrefix(new ImageFilePath(PREFIX));

        assertTrue(
            s3Client
                .listObjectsV2(ListObjectsV2Request.builder().bucket(bucket).prefix(PREFIX).build())
                .contents()
                .isEmpty());
        // プレフィックス配下でないオブジェクトは削除されないこと
        assertDoesNotThrow(
            () ->
                s3Client.headObject(
                    HeadObjectRequest.builder().bucket(bucket).key(keyOutsidePrefix).build()));
      } finally {
        deleteQuietly(keyOutsidePrefix);
      }
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getPresignedUrl {
    private static final String KEY = "integration-test/presigned-target.jpg";

    @AfterEach
    void tearDown() {
      s3Client.deleteObject(builder -> builder.bucket(bucket).key(KEY));
    }

    @Test
    @Order(1)
    @DisplayName("正常系：発行された署名付きURLへの実際のHTTP GETで、保存したファイルの内容を取得できること")
    void getPresignedUrl_returnsDownloadableUrl() throws IOException, InterruptedException {
      s3Client.putObject(
          PutObjectRequest.builder().bucket(bucket).key(KEY).build(),
          software.amazon.awssdk.core.sync.RequestBody.fromBytes(JPEG_BYTES));

      ImageFilePath presignedUrl = fileRepository.getPresignedUrl(new ImageFilePath(KEY));

      assertTrue(presignedUrl.value().contains("X-Amz-Signature"));

      HttpClient httpClient = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder(URI.create(presignedUrl.value())).GET().build();
      HttpResponse<byte[]> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

      assertEquals(200, response.statusCode());
      assertArrayEquals(JPEG_BYTES, response.body());
    }
  }
}
