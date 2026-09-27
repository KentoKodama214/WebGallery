package com.web.gallery.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;

import com.web.gallery.infrastructure.web.CorsConfig;
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
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ProdConfigValidationRunnerTest {

  /** {@code X-Forwarded-For}を信頼する送信元範囲のプロパティキー */
  private static final String INTERNAL_PROXIES_PROPERTY = "server.tomcat.remoteip.internal-proxies";

  /** 本番として妥当な信頼プロキシ範囲（ALBのサブネットCIDR相当） */
  private static final String VALID_INTERNAL_PROXIES = "10\\.0\\.1\\.\\d{1,3}";

  private ProdConfigValidationRunner prodConfigValidationRunner;

  private MockEnvironment environment;

  @Mock private CorsConfig corsConfig;

  @Mock private S3Config s3Config;

  @Mock private DataSourceProperties dataSourceProperties;

  @Mock private DataSourceReplicaConfig dataSourceReplicaConfig;

  @BeforeEach
  void setUp() {
    environment = new MockEnvironment();
    // 信頼プロキシ範囲以外の検証に集中できるよう、既定では妥当な値を設定しておく
    environment.setProperty(INTERNAL_PROXIES_PROPERTY, VALID_INTERNAL_PROXIES);
    prodConfigValidationRunner =
        new ProdConfigValidationRunner(
            corsConfig, s3Config, dataSourceProperties, dataSourceReplicaConfig, environment);
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

    @Nested
    @Order(4)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("信頼プロキシ範囲（TRUSTED_PROXIES）の検証")
    class trustedProxies {

      @BeforeEach
      void stubValidCors() {
        lenient()
            .when(corsConfig.getAllowedOrigins())
            .thenReturn(List.of("https://gallery.example.com"));
      }

      @Test
      @Order(1)
      @DisplayName("前段プロキシのCIDRに絞られていれば検証を通過する")
      void narrowedCidr() {
        environment.setProperty(INTERNAL_PROXIES_PROPERTY, VALID_INTERNAL_PROXIES);

        assertDoesNotThrow(() -> prodConfigValidationRunner.validate());
      }

      @Test
      @Order(2)
      @DisplayName("未設定なら起動失敗する")
      void notConfigured() {
        // プロパティを一切持たないEnvironmentで組み直し、「キー自体が存在しない」状態を再現する
        ProdConfigValidationRunner runnerWithoutProperty =
            new ProdConfigValidationRunner(
                corsConfig,
                s3Config,
                dataSourceProperties,
                dataSourceReplicaConfig,
                new MockEnvironment());

        assertThrows(IllegalStateException.class, runnerWithoutProperty::validate);
      }

      @Test
      @Order(3)
      @DisplayName("空白のみなら起動失敗する")
      void blank() {
        environment.setProperty(INTERNAL_PROXIES_PROPERTY, "   ");

        assertThrows(IllegalStateException.class, () -> prodConfigValidationRunner.validate());
      }

      @Test
      @Order(4)
      @DisplayName("任意のIPに一致する catch-all（.*）は起動失敗する")
      void catchAllAnyChar() {
        environment.setProperty(INTERNAL_PROXIES_PROPERTY, ".*");

        assertThrows(IllegalStateException.class, () -> prodConfigValidationRunner.validate());
      }

      @Test
      @Order(5)
      @DisplayName("前後に空白を含む catch-all（.+）も起動失敗する")
      void catchAllWithSurroundingWhitespace() {
        environment.setProperty(INTERNAL_PROXIES_PROPERTY, "  .+  ");

        assertThrows(IllegalStateException.class, () -> prodConfigValidationRunner.validate());
      }
    }
  }
}
