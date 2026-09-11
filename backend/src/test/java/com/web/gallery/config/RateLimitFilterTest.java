package com.web.gallery.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.web.gallery.helper.RateLimiter;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;

/** {@link RateLimitFilter} のユニットテスト */
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

  @Mock private RateLimiter rateLimiter;

  private RateLimitFilter rateLimitFilter;

  private static RateLimitConfig config(boolean enabled) {
    return new RateLimitConfig(
        enabled,
        new RateLimitConfig.Bucket(10, 60),
        new RateLimitConfig.Bucket(5, 3600),
        new RateLimitConfig.Bucket(240, 60));
  }

  @BeforeEach
  void setUp() {
    rateLimitFilter = new RateLimitFilter(config(true), rateLimiter);
  }

  private MockHttpServletRequest request(String method, String uri) {
    MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
    request.setRemoteAddr("203.0.113.9");
    return request;
  }

  @Test
  @DisplayName("上限内なら後続フィルターへ処理を渡す")
  void passesThroughWhenUnderLimit() throws Exception {
    when(rateLimiter.tryAcquire(any(), any())).thenReturn(true);
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    rateLimitFilter.doFilter(request("GET", "/api/v1/accounts"), response, chain);

    assertEquals(200, response.getStatus());
    assertNotNull(chain.getRequest());
    assertEquals("/api/v1/accounts", ((MockHttpServletRequest) chain.getRequest()).getRequestURI());
  }

  @Test
  @DisplayName("上限超過なら429を返し後続フィルターを呼ばない")
  void blocksWhenOverLimit() throws Exception {
    when(rateLimiter.tryAcquire(any(), any())).thenReturn(false);
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);

    rateLimitFilter.doFilter(request("POST", "/api/v1/auth/login"), response, chain);

    assertEquals(429, response.getStatus());
    assertEquals("60", response.getHeader("Retry-After"));
    assertTrue(response.getContentAsString().contains("E-A-0003"));
    verify(chain, never()).doFilter(any(), any());
  }

  @Test
  @DisplayName("ログインパスは AUTH カテゴリのキーで判定する")
  void loginUsesAuthCategory() throws Exception {
    when(rateLimiter.tryAcquire(eq("203.0.113.9|AUTH"), any())).thenReturn(true);
    MockHttpServletResponse response = new MockHttpServletResponse();

    rateLimitFilter.doFilter(
        request("POST", "/api/v1/auth/login"), response, new MockFilterChain());

    verify(rateLimiter).tryAcquire(eq("203.0.113.9|AUTH"), any());
  }

  @Test
  @DisplayName("アカウント登録POSTは REGISTER カテゴリのキーで判定する")
  void registerUsesRegisterCategory() throws Exception {
    when(rateLimiter.tryAcquire(eq("203.0.113.9|REGISTER"), any())).thenReturn(true);
    MockHttpServletResponse response = new MockHttpServletResponse();

    rateLimitFilter.doFilter(request("POST", "/api/v1/accounts"), response, new MockFilterChain());

    verify(rateLimiter).tryAcquire(eq("203.0.113.9|REGISTER"), any());
  }

  @Test
  @DisplayName("トークンリフレッシュは GENERAL カテゴリのキーで判定する")
  void refreshUsesGeneralCategory() throws Exception {
    when(rateLimiter.tryAcquire(eq("203.0.113.9|GENERAL"), any())).thenReturn(true);
    MockHttpServletResponse response = new MockHttpServletResponse();

    rateLimitFilter.doFilter(
        request("POST", "/api/v1/auth/refresh"), response, new MockFilterChain());

    verify(rateLimiter).tryAcquire(eq("203.0.113.9|GENERAL"), any());
  }

  @Test
  @DisplayName("無効化されている場合はカウントせず素通しする")
  void skipsWhenDisabled() throws Exception {
    rateLimitFilter = new RateLimitFilter(config(false), rateLimiter);
    MockHttpServletResponse response = new MockHttpServletResponse();

    rateLimitFilter.doFilter(
        request("POST", "/api/v1/auth/login"), response, new MockFilterChain());

    verify(rateLimiter, never()).tryAcquire(any(), any());
  }

  @Test
  @DisplayName("/api/** 以外のパスはカウントせず素通しする")
  void skipsNonApiPath() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();

    rateLimitFilter.doFilter(request("GET", "/scalar"), response, new MockFilterChain());

    verify(rateLimiter, never()).tryAcquire(any(), any());
  }
}
