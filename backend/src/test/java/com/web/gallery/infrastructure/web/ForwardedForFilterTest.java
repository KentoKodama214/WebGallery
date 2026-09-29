package com.web.gallery.infrastructure.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;

/**
 * {@link ForwardedForFilter}のテストクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ForwardedForFilterTest {

  /** 前段プロキシ（ALB）が存在するサブネット相当の信頼範囲 */
  private static final String TRUSTED_PROXIES = "10\\.0\\.1\\.\\d{1,3}";

  /**
   * フィルターを通したあとに後続へ渡ったリクエストの送信元IPを返す
   *
   * @param trustedProxies 信頼する送信元の正規表現
   * @param remoteAddr TCP接続の送信元IP
   * @param forwardedFor {@code X-Forwarded-For}ヘッダーの値（nullならヘッダーなし）
   * @return 後続のフィルタ・コントローラが見る送信元IP
   * @throws ServletException フィルター内で例外が発生した場合
   * @throws IOException フィルター内で入出力例外が発生した場合
   */
  private String resolvedRemoteAddr(String trustedProxies, String remoteAddr, String forwardedFor)
      throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr(remoteAddr);
    if (forwardedFor != null) {
      request.addHeader("X-Forwarded-For", forwardedFor);
    }
    MockFilterChain filterChain = new MockFilterChain();

    new ForwardedForFilter(new TrustedProxyConfig(trustedProxies))
        .doFilter(request, new MockHttpServletResponse(), filterChain);

    return filterChain.getRequest().getRemoteAddr();
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @DisplayName("信頼できるプロキシ経由の場合")
  class fromTrustedProxy {

    @Test
    @Order(1)
    @DisplayName("X-Forwarded-Forが1件なら、その値を送信元IPとする")
    void singleEntry() throws Exception {
      assertEquals("203.0.113.9", resolvedRemoteAddr(TRUSTED_PROXIES, "10.0.1.5", "203.0.113.9"));
    }

    @Test
    @Order(2)
    @DisplayName("右端から信頼できるプロキシを読み飛ばし、最初に現れた値を送信元IPとする")
    void skipsTrailingTrustedProxies() throws Exception {
      // 左端はクライアントが詐称した値、中央が実クライアントIP、右端がALBのIP
      assertEquals(
          "203.0.113.9",
          resolvedRemoteAddr(TRUSTED_PROXIES, "10.0.1.5", "1.1.1.1, 203.0.113.9, 10.0.1.9"));
    }

    @Test
    @Order(3)
    @DisplayName("複数行に分かれたX-Forwarded-Forも1つのチェーンとして扱う")
    void multipleHeaderLines() throws Exception {
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setRemoteAddr("10.0.1.5");
      request.addHeader("X-Forwarded-For", "1.1.1.1");
      request.addHeader("X-Forwarded-For", "203.0.113.9, 10.0.1.9");
      MockFilterChain filterChain = new MockFilterChain();

      new ForwardedForFilter(new TrustedProxyConfig(TRUSTED_PROXIES))
          .doFilter(request, new MockHttpServletResponse(), filterChain);

      assertEquals("203.0.113.9", filterChain.getRequest().getRemoteAddr());
    }

    @Test
    @Order(4)
    @DisplayName("空要素は無視する")
    void ignoresEmptyEntries() throws Exception {
      assertEquals(
          "203.0.113.9", resolvedRemoteAddr(TRUSTED_PROXIES, "10.0.1.5", " , 203.0.113.9 , "));
    }

    @Test
    @Order(5)
    @DisplayName("X-Forwarded-Forが無ければ接続元IPのままにする")
    void noHeader() throws Exception {
      assertEquals("10.0.1.5", resolvedRemoteAddr(TRUSTED_PROXIES, "10.0.1.5", null));
    }

    @Test
    @Order(6)
    @DisplayName("全要素が信頼できるプロキシなら接続元IPのままにする")
    void allEntriesAreTrustedProxies() throws Exception {
      assertEquals(
          "10.0.1.5", resolvedRemoteAddr(TRUSTED_PROXIES, "10.0.1.5", "10.0.1.8, 10.0.1.9"));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @DisplayName("信頼できない送信元の場合")
  class fromUntrustedSource {

    @Test
    @Order(1)
    @DisplayName("X-Forwarded-Forを詐称しても無視し、TCP接続の実IPを送信元IPとする")
    void ignoresSpoofedForwardedFor() throws Exception {
      // 信頼範囲外（インターネット等）から直接届いたリクエスト
      assertEquals(
          "198.51.100.7", resolvedRemoteAddr(TRUSTED_PROXIES, "198.51.100.7", "1.1.1.1, 2.2.2.2"));
    }

    @Test
    @Order(2)
    @DisplayName("信頼する範囲が未設定なら、どの送信元からのX-Forwarded-Forも信用しない")
    void trustedProxiesNotConfigured() throws Exception {
      assertEquals("10.0.1.5", resolvedRemoteAddr(null, "10.0.1.5", "203.0.113.9"));
    }

    @Test
    @Order(3)
    @DisplayName("信頼する範囲が正規表現として不正なら、どの送信元からのX-Forwarded-Forも信用しない")
    void trustedProxiesInvalidRegex() throws Exception {
      // 起動時検証（prodプロファイル）で弾く想定だが、万一通ってしまっても安全側に倒れること
      assertEquals("10.0.1.5", resolvedRemoteAddr("10\\.0\\.[", "10.0.1.5", "203.0.113.9"));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @DisplayName("TrustedProxyConfigの正規表現")
  class trustedProxyPattern {

    @Test
    @Order(1)
    @DisplayName("未設定・空白のみ・不正な正規表現はnullを返す")
    void invalidValuesReturnNull() {
      assertEquals(null, new TrustedProxyConfig(null).trustedProxyPattern());
      assertEquals(null, new TrustedProxyConfig("   ").trustedProxyPattern());
      assertEquals(null, new TrustedProxyConfig("10\\.0\\.[").trustedProxyPattern());
    }

    @Test
    @Order(2)
    @DisplayName("前後の空白をtrimしてコンパイルする")
    void trimsSurroundingWhitespace() {
      assertEquals(
          TRUSTED_PROXIES,
          new TrustedProxyConfig("  " + TRUSTED_PROXIES + "  ").trustedProxyPattern().pattern());
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @DisplayName("後続へ渡すリクエスト")
  class wrappedRequest {

    @Test
    @Order(1)
    @DisplayName("getRemoteHostも実クライアントIPを返す")
    void remoteHostIsAlsoReplaced() throws Exception {
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setRemoteAddr("10.0.1.5");
      request.setRemoteHost("alb.internal");
      request.addHeader("X-Forwarded-For", "203.0.113.9");
      MockFilterChain filterChain = new MockFilterChain();

      new ForwardedForFilter(new TrustedProxyConfig(TRUSTED_PROXIES))
          .doFilter(request, new MockHttpServletResponse(), filterChain);

      assertEquals("203.0.113.9", filterChain.getRequest().getRemoteHost());
    }

    @Test
    @Order(2)
    @DisplayName("差し替えが不要な場合は元のリクエストをそのまま渡す")
    void passesOriginalRequestWhenNoChange() throws Exception {
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setRemoteAddr("198.51.100.7");
      request.addHeader("X-Forwarded-For", "1.1.1.1");
      MockFilterChain filterChain = new MockFilterChain();

      new ForwardedForFilter(new TrustedProxyConfig(TRUSTED_PROXIES))
          .doFilter(request, new MockHttpServletResponse(), filterChain);

      assertEquals(request, filterChain.getRequest());
    }

    @Test
    @Order(3)
    @DisplayName("MockFilterChainは1度しか呼べない（フィルターが多重にチェーンを進めていないことの確認）")
    void chainIsCalledOnce() throws Exception {
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setRemoteAddr("10.0.1.5");
      request.addHeader("X-Forwarded-For", "203.0.113.9");
      MockFilterChain filterChain = new MockFilterChain();
      ForwardedForFilter filter = new ForwardedForFilter(new TrustedProxyConfig(TRUSTED_PROXIES));

      filter.doFilter(request, new MockHttpServletResponse(), filterChain);

      // 2度目の doFilter は MockFilterChain 側で拒否される（1度しか進めていないことの裏取り）
      assertThrows(
          IllegalStateException.class,
          () -> filterChain.doFilter(request, new MockHttpServletResponse()));
    }
  }
}
