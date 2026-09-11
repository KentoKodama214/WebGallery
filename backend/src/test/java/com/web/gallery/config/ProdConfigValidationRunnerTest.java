package com.web.gallery.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ProdConfigValidationRunnerTest {

  @InjectMocks private ProdConfigValidationRunner prodConfigValidationRunner;

  @Mock private CorsConfig corsConfig;

  @Mock private S3Config s3Config;

  @Nested
  @DisplayName("CORS許可オリジンの検証")
  class CorsAllowedOrigins {

    @Test
    @DisplayName("https の単一オリジンなら検証を通過する")
    void httpsSingleOrigin() {
      lenient()
          .when(corsConfig.getAllowedOrigins())
          .thenReturn(List.of("https://gallery.example.com"));

      assertDoesNotThrow(() -> prodConfigValidationRunner.validate());
    }

    @Test
    @DisplayName("オリジンが未設定なら起動失敗する")
    void empty() {
      lenient().when(corsConfig.getAllowedOrigins()).thenReturn(List.of());

      assertThrows(IllegalStateException.class, () -> prodConfigValidationRunner.validate());
    }

    @Test
    @DisplayName("http のオリジンは起動失敗する")
    void plainHttp() {
      lenient()
          .when(corsConfig.getAllowedOrigins())
          .thenReturn(List.of("http://gallery.example.com"));

      assertThrows(IllegalStateException.class, () -> prodConfigValidationRunner.validate());
    }

    @Test
    @DisplayName("ワイルドカードを含むオリジンは起動失敗する")
    void wildcard() {
      lenient().when(corsConfig.getAllowedOrigins()).thenReturn(List.of("https://*.example.com"));

      assertThrows(IllegalStateException.class, () -> prodConfigValidationRunner.validate());
    }

    @Test
    @DisplayName("パスを含むオリジンは起動失敗する")
    void withPath() {
      lenient()
          .when(corsConfig.getAllowedOrigins())
          .thenReturn(List.of("https://gallery.example.com/app"));

      assertThrows(IllegalStateException.class, () -> prodConfigValidationRunner.validate());
    }
  }

  @Nested
  @DisplayName("S3関連URLの検証")
  class S3Urls {

    @Test
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
    @DisplayName("http のエンドポイントは起動失敗する")
    void plainHttpEndpoint() {
      lenient()
          .when(corsConfig.getAllowedOrigins())
          .thenReturn(List.of("https://gallery.example.com"));
      lenient().when(s3Config.getEndpoint()).thenReturn("http://minio.internal:9000");

      assertThrows(IllegalStateException.class, () -> prodConfigValidationRunner.validate());
    }

    @Test
    @DisplayName("http の公開ベースURLは起動失敗する")
    void plainHttpPublicBaseUrl() {
      lenient()
          .when(corsConfig.getAllowedOrigins())
          .thenReturn(List.of("https://gallery.example.com"));
      lenient().when(s3Config.getPublicBaseUrl()).thenReturn("http://cdn.example.com");

      assertThrows(IllegalStateException.class, () -> prodConfigValidationRunner.validate());
    }

    @Test
    @DisplayName("https のエンドポイント・公開ベースURLは検証を通過する")
    void httpsUrls() {
      lenient()
          .when(corsConfig.getAllowedOrigins())
          .thenReturn(List.of("https://gallery.example.com"));
      lenient().when(s3Config.getEndpoint()).thenReturn("https://s3.ap-northeast-1.amazonaws.com");
      lenient().when(s3Config.getPublicBaseUrl()).thenReturn("https://cdn.example.com");

      assertDoesNotThrow(() -> prodConfigValidationRunner.validate());
    }
  }
}
