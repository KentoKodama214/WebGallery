package com.web.gallery.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class S3ClientConfigTest {

  @Mock private S3Config s3Config;

  private void stubCommonProperties() {
    doReturn("ap-northeast-1").when(s3Config).getRegion();
    doReturn("access-key").when(s3Config).getAccessKey();
    doReturn("secret-key").when(s3Config).getSecretKey();
    doReturn(Boolean.TRUE).when(s3Config).getPathStyleAccess();
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class s3Client {
    @Test
    @Order(1)
    @DisplayName("正常系：エンドポイントが設定されている場合、S3Clientを生成できること")
    void s3Client_withEndpoint() {
      stubCommonProperties();
      doReturn("http://localhost:9000").when(s3Config).getEndpoint();

      S3ClientConfig s3ClientConfig = new S3ClientConfig(s3Config);

      S3Client actual = s3ClientConfig.s3Client();

      assertNotNull(actual);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：エンドポイントが未設定（null）の場合、S3Clientを生成できること")
    void s3Client_nullEndpoint() {
      stubCommonProperties();
      doReturn(null).when(s3Config).getEndpoint();

      S3ClientConfig s3ClientConfig = new S3ClientConfig(s3Config);

      S3Client actual = s3ClientConfig.s3Client();

      assertNotNull(actual);
    }

    @Test
    @Order(3)
    @DisplayName("正常系：エンドポイントが空文字の場合、S3Clientを生成できること")
    void s3Client_blankEndpoint() {
      stubCommonProperties();
      doReturn("").when(s3Config).getEndpoint();

      S3ClientConfig s3ClientConfig = new S3ClientConfig(s3Config);

      S3Client actual = s3ClientConfig.s3Client();

      assertNotNull(actual);
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class s3Presigner {
    @Test
    @Order(1)
    @DisplayName("正常系：エンドポイントが設定されている場合、S3Presignerを生成できること")
    void s3Presigner_withEndpoint() {
      stubCommonProperties();
      doReturn("http://localhost:9000").when(s3Config).getEndpoint();

      S3ClientConfig s3ClientConfig = new S3ClientConfig(s3Config);

      S3Presigner actual = s3ClientConfig.s3Presigner();

      assertNotNull(actual);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：エンドポイントが未設定（null）の場合、S3Presignerを生成できること")
    void s3Presigner_nullEndpoint() {
      stubCommonProperties();
      doReturn(null).when(s3Config).getEndpoint();

      S3ClientConfig s3ClientConfig = new S3ClientConfig(s3Config);

      S3Presigner actual = s3ClientConfig.s3Presigner();

      assertNotNull(actual);
    }
  }
}
