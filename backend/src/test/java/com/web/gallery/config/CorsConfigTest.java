package com.web.gallery.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class CorsConfigTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class validateAllowedOrigins {
    @Test
    @Order(1)
    @DisplayName("単一の正常なオリジンなら検証を通過する")
    void singleValidOrigin() {
      CorsConfig corsConfig = new CorsConfig(List.of("https://gallery.example.com"));

      assertDoesNotThrow(corsConfig::validateAllowedOrigins);
    }

    @Test
    @Order(2)
    @DisplayName("オリジンが未設定なら起動失敗する")
    void empty() {
      CorsConfig corsConfig = new CorsConfig(List.of());

      assertThrows(IllegalStateException.class, corsConfig::validateAllowedOrigins);
    }

    @Test
    @Order(3)
    @DisplayName("null のオリジンリストは起動失敗する")
    void nullList() {
      CorsConfig corsConfig = new CorsConfig(null);

      assertThrows(IllegalStateException.class, corsConfig::validateAllowedOrigins);
    }

    @Test
    @Order(4)
    @DisplayName("カンマ区切りで複数オリジンを1要素に詰めた誤設定は起動失敗する")
    void commaSeparatedInSingleEntry() {
      CorsConfig corsConfig =
          new CorsConfig(List.of("https://a.example.com,https://b.example.com"));

      assertThrows(IllegalStateException.class, corsConfig::validateAllowedOrigins);
    }

    @Test
    @Order(5)
    @DisplayName("空白を含むオリジンは起動失敗する")
    void whitespaceInEntry() {
      CorsConfig corsConfig = new CorsConfig(List.of("https://gallery.example.com "));

      assertThrows(IllegalStateException.class, corsConfig::validateAllowedOrigins);
    }

    @Test
    @Order(6)
    @DisplayName("全要素が空白のみのオリジンリストは起動失敗する")
    void allBlankEntries() {
      CorsConfig corsConfig = new CorsConfig(List.of("   "));

      assertThrows(IllegalStateException.class, corsConfig::validateAllowedOrigins);
    }

    @Test
    @Order(7)
    @DisplayName("有効なオリジンと空文字が混在する場合は起動失敗する")
    void mixedValidAndBlankEntry() {
      CorsConfig corsConfig = new CorsConfig(List.of("https://gallery.example.com", ""));

      assertThrows(IllegalStateException.class, corsConfig::validateAllowedOrigins);
    }
  }
}
