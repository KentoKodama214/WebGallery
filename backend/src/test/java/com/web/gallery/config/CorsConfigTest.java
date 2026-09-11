package com.web.gallery.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class CorsConfigTest {

  @Test
  @DisplayName("単一の正常なオリジンなら検証を通過する")
  void singleValidOrigin() {
    CorsConfig corsConfig = new CorsConfig(List.of("https://gallery.example.com"));

    assertDoesNotThrow(corsConfig::validateAllowedOrigins);
  }

  @Test
  @DisplayName("オリジンが未設定なら起動失敗する")
  void empty() {
    CorsConfig corsConfig = new CorsConfig(List.of());

    assertThrows(IllegalStateException.class, corsConfig::validateAllowedOrigins);
  }

  @Test
  @DisplayName("null のオリジンリストは起動失敗する")
  void nullList() {
    CorsConfig corsConfig = new CorsConfig(null);

    assertThrows(IllegalStateException.class, corsConfig::validateAllowedOrigins);
  }

  @Test
  @DisplayName("カンマ区切りで複数オリジンを1要素に詰めた誤設定は起動失敗する")
  void commaSeparatedInSingleEntry() {
    CorsConfig corsConfig = new CorsConfig(List.of("https://a.example.com,https://b.example.com"));

    assertThrows(IllegalStateException.class, corsConfig::validateAllowedOrigins);
  }

  @Test
  @DisplayName("空白を含むオリジンは起動失敗する")
  void whitespaceInEntry() {
    CorsConfig corsConfig = new CorsConfig(List.of("https://gallery.example.com "));

    assertThrows(IllegalStateException.class, corsConfig::validateAllowedOrigins);
  }
}
