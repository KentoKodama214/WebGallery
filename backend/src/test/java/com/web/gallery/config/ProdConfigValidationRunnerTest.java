package com.web.gallery.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ProdConfigValidationRunnerTest {

  private ProdConfigValidationRunner prodConfigValidationRunner;

  @Mock private CorsConfig corsConfig;

  @Mock private S3Config s3Config;

  @Mock private DataSourceProperties dataSourceProperties;

  @Mock private DataSourceReplicaConfig dataSourceReplicaConfig;

  @BeforeEach
  void setUp() {
    prodConfigValidationRunner =
        new ProdConfigValidationRunner(
            corsConfig, s3Config, dataSourceProperties, dataSourceReplicaConfig);
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class validate {
    @Nested
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("CORS許可オリジンの検証")
    class corsAllowedOrigins {

      @Test
      @Order(1)
      @DisplayName("https の単一オリジンなら検証を通過する")
      void httpsSingleOrigin() {
        lenient()
            .when(corsConfig.getAllowedOrigins())
            .thenReturn(List.of("https://gallery.example.com"));

        assertDoesNotThrow(() -> prodConfigValidationRunner.validate());
      }

      @Test
      @Order(2)
      @DisplayName("オリジンが未設定なら起動失敗する")
      void empty() {
        lenient().when(corsConfig.getAllowedOrigins()).thenReturn(List.of());

        assertThrows(IllegalStateException.class, () -> prodConfigValidationRunner.validate());
      }

      @Test
      @Order(3)
      @DisplayName("http のオリジンは起動失敗する")
      void plainHttp() {
        lenient()
            .when(corsConfig.getAllowedOrigins())
            .thenReturn(List.of("http://gallery.example.com"));

        assertThrows(IllegalStateException.class, () -> prodConfigValidationRunner.validate());
      }

      @Test
      @Order(4)
      @DisplayName("ワイルドカードを含むオリジンは起動失敗する")
      void wildcard() {
        lenient().when(corsConfig.getAllowedOrigins()).thenReturn(List.of("https://*.example.com"));

        assertThrows(IllegalStateException.class, () -> prodConfigValidationRunner.validate());
      }

      @Test
      @Order(5)
      @DisplayName("パスを含むオリジンは起動失敗する")
      void withPath() {
        lenient()
            .when(corsConfig.getAllowedOrigins())
            .thenReturn(List.of("https://gallery.example.com/app"));

        assertThrows(IllegalStateException.class, () -> prodConfigValidationRunner.validate());
      }

      @Test
      @Order(6)
      @DisplayName("空白のみのオリジンのみを含むリストは起動失敗する")
      void blankOnlyList() {
        lenient().when(corsConfig.getAllowedOrigins()).thenReturn(List.of("   "));

        assertThrows(IllegalStateException.class, () -> prodConfigValidationRunner.validate());
      }

      @Test
      @Order(7)
      @DisplayName("複数オリジンのうち一部が空白の場合は起動失敗する")
      void partiallyBlankInMultipleOrigins() {
        lenient()
            .when(corsConfig.getAllowedOrigins())
            .thenReturn(List.of("https://gallery.example.com", "   "));

        assertThrows(IllegalStateException.class, () -> prodConfigValidationRunner.validate());
      }

      @Test
      @Order(8)
      @DisplayName("ホストを含まないオリジンは起動失敗する")
      void noHost() {
        lenient().when(corsConfig.getAllowedOrigins()).thenReturn(List.of("https:///path"));

        assertThrows(IllegalStateException.class, () -> prodConfigValidationRunner.validate());
      }

      @Test
      @Order(9)
      @DisplayName("末尾がルートパス（/）のみのオリジンは検証を通過する")
      void rootPathOnly() {
        lenient()
            .when(corsConfig.getAllowedOrigins())
            .thenReturn(List.of("https://gallery.example.com/"));

        assertDoesNotThrow(() -> prodConfigValidationRunner.validate());
      }

      @Test
      @Order(10)
      @DisplayName("クエリを含むオリジンは起動失敗する")
      void withQuery() {
        lenient()
            .when(corsConfig.getAllowedOrigins())
            .thenReturn(List.of("https://gallery.example.com?q=1"));

        assertThrows(IllegalStateException.class, () -> prodConfigValidationRunner.validate());
      }

      @Test
      @Order(11)
      @DisplayName("フラグメントを含むオリジンは起動失敗する")
      void withFragment() {
        lenient()
            .when(corsConfig.getAllowedOrigins())
            .thenReturn(List.of("https://gallery.example.com#frag"));

        assertThrows(IllegalStateException.class, () -> prodConfigValidationRunner.validate());
      }

      @Test
      @Order(12)
      @DisplayName("URIとして不正な形式のオリジンは起動失敗する")
      void invalidUriSyntax() {
        lenient().when(corsConfig.getAllowedOrigins()).thenReturn(List.of("https://exa mple.com"));

        assertThrows(IllegalStateException.class, () -> prodConfigValidationRunner.validate());
      }
    }

    @Nested
    @Order(2)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("S3関連URLの検証")
    class s3Urls {

      @Test
      @Order(1)
      @DisplayName("エンドポイント・公開ベースURLが未設定なら検証を通過する（AWS S3既定利用）")
      void blankIsAllowed() {
        lenient()
            .when(corsConfig.getAllowedOrigins())
            .thenReturn(List.of("https://gallery.example.com"));
        lenient().when(s3Config.getEndpoint()).thenReturn("");
        lenient().when(s3Config.getPublicBaseUrl()).thenReturn(null);

        assertDoesNotThrow(() -> prodConfigValidationRunner.validate());
      }

      @Test
      @Order(2)
      @DisplayName("http のエンドポイントは起動失敗する")
      void plainHttpEndpoint() {
        lenient()
            .when(corsConfig.getAllowedOrigins())
            .thenReturn(List.of("https://gallery.example.com"));
        lenient().when(s3Config.getEndpoint()).thenReturn("http://minio.internal:9000");

        assertThrows(IllegalStateException.class, () -> prodConfigValidationRunner.validate());
      }

      @Test
      @Order(3)
      @DisplayName("http の公開ベースURLは起動失敗する")
      void plainHttpPublicBaseUrl() {
        lenient()
            .when(corsConfig.getAllowedOrigins())
            .thenReturn(List.of("https://gallery.example.com"));
        lenient().when(s3Config.getPublicBaseUrl()).thenReturn("http://cdn.example.com");

        assertThrows(IllegalStateException.class, () -> prodConfigValidationRunner.validate());
      }

      @Test
      @Order(4)
      @DisplayName("https のエンドポイント・公開ベースURLは検証を通過する")
      void httpsUrls() {
        lenient()
            .when(corsConfig.getAllowedOrigins())
            .thenReturn(List.of("https://gallery.example.com"));
        lenient()
            .when(s3Config.getEndpoint())
            .thenReturn("https://s3.ap-northeast-1.amazonaws.com");
        lenient().when(s3Config.getPublicBaseUrl()).thenReturn("https://cdn.example.com");

        assertDoesNotThrow(() -> prodConfigValidationRunner.validate());
      }
    }

    @Nested
    @Order(3)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("リードレプリカ接続URLの検証")
    class replicaUrl {

      @Test
      @Order(1)
      @DisplayName("未設定なら検証を通過する")
      void notConfigured() {
        lenient()
            .when(corsConfig.getAllowedOrigins())
            .thenReturn(List.of("https://gallery.example.com"));
        lenient().when(dataSourceReplicaConfig.getUrl()).thenReturn(null);

        assertDoesNotThrow(() -> prodConfigValidationRunner.validate());
      }

      @Test
      @Order(2)
      @DisplayName("jdbc:postgresql:// で始まらないURLは起動失敗する")
      void notPostgresqlUrl() {
        lenient()
            .when(corsConfig.getAllowedOrigins())
            .thenReturn(List.of("https://gallery.example.com"));
        lenient().when(dataSourceReplicaConfig.getUrl()).thenReturn("jdbc:mysql://replica:3306/db");

        assertThrows(IllegalStateException.class, () -> prodConfigValidationRunner.validate());
      }

      @Test
      @Order(3)
      @DisplayName("プライマリと同一URLは起動失敗する")
      void sameAsPrimary() {
        lenient()
            .when(corsConfig.getAllowedOrigins())
            .thenReturn(List.of("https://gallery.example.com"));
        lenient()
            .when(dataSourceProperties.getUrl())
            .thenReturn("jdbc:postgresql://primary:5432/db");
        lenient()
            .when(dataSourceReplicaConfig.getUrl())
            .thenReturn("jdbc:postgresql://primary:5432/db");

        assertThrows(IllegalStateException.class, () -> prodConfigValidationRunner.validate());
      }

      @Test
      @Order(4)
      @DisplayName("プライマリと異なるPostgreSQL接続URLなら検証を通過する")
      void validReplicaUrl() {
        lenient()
            .when(corsConfig.getAllowedOrigins())
            .thenReturn(List.of("https://gallery.example.com"));
        lenient()
            .when(dataSourceProperties.getUrl())
            .thenReturn("jdbc:postgresql://primary:5432/db");
        lenient()
            .when(dataSourceReplicaConfig.getUrl())
            .thenReturn("jdbc:postgresql://replica:5432/db");

        assertDoesNotThrow(() -> prodConfigValidationRunner.validate());
      }
    }
  }
}
