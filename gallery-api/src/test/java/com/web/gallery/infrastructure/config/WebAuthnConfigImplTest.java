package com.web.gallery.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class WebAuthnConfigImplTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class validate {
    @Test
    @Order(1)
    @DisplayName("RP IDとoriginのホストが一致すれば検証を通過する")
    void sameHost() {
      assertDoesNotThrow(() ->
          new WebAuthnConfigImpl("localhost", "WebGallery", List.of("http://localhost:3000"), 300)
              .validate());
    }

    @Test
    @Order(2)
    @DisplayName("originのホストがRP IDのサブドメインでも検証を通過する")
    void subdomain() {
      assertDoesNotThrow(() ->
          new WebAuthnConfigImpl(
                  "example.com", "WebGallery", List.of("https://gallery.example.com"), 300)
              .validate());
    }

    static java.util.stream.Stream<WebAuthnConfigImpl> invalidConfigs() {
      return java.util.stream.Stream.of(
          // RP IDが未設定・空白・スキームやポートを含む
          new WebAuthnConfigImpl(null, "WebGallery", List.of("http://localhost:3000"), 300),
          new WebAuthnConfigImpl(" ", "WebGallery", List.of("http://localhost:3000"), 300),
          new WebAuthnConfigImpl(
              "https://localhost", "WebGallery", List.of("http://localhost:3000"), 300),
          new WebAuthnConfigImpl(
              "localhost:3000", "WebGallery", List.of("http://localhost:3000"), 300),
          // RP名が未設定
          new WebAuthnConfigImpl("localhost", " ", List.of("http://localhost:3000"), 300),
          // タイムアウトが未設定・0以下
          new WebAuthnConfigImpl("localhost", "WebGallery", List.of("http://localhost:3000"), null),
          new WebAuthnConfigImpl("localhost", "WebGallery", List.of("http://localhost:3000"), 0),
          // originが未設定・空白のみ
          new WebAuthnConfigImpl("localhost", "WebGallery", List.of(), 300),
          new WebAuthnConfigImpl("localhost", "WebGallery", null, 300),
          new WebAuthnConfigImpl("localhost", "WebGallery", List.of(" "), 300),
          // originのホストがRP IDと一致せず、サブドメインでもない
          new WebAuthnConfigImpl("example.com", "WebGallery", List.of("https://evil.com"), 300),
          new WebAuthnConfigImpl(
              "example.com", "WebGallery", List.of("https://notexample.com"), 300),
          // originがパスを含む・オリジンとして不正
          new WebAuthnConfigImpl(
              "localhost", "WebGallery", List.of("http://localhost:3000/login"), 300),
          new WebAuthnConfigImpl("localhost", "WebGallery", List.of("localhost"), 300),
          new WebAuthnConfigImpl("localhost", "WebGallery", List.of("http://local host"), 300));
    }

    @ParameterizedTest
    @MethodSource("invalidConfigs")
    @Order(3)
    @DisplayName("RP ID・RP名・タイムアウト・originのいずれかが不正なら起動失敗する")
    void invalid(WebAuthnConfigImpl config) {
      assertThrows(IllegalStateException.class, config::validate);
    }
  }
}
