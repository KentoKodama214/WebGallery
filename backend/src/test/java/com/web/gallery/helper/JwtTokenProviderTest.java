package com.web.gallery.helper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

import com.web.gallery.AccountPrincipal;
import com.web.gallery.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

  private JwtTokenProvider jwtTokenProvider;

  @Mock private AccountPrincipal principal;

  private static final String SECRET =
      "test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm";
  private static final Integer ACCESS_TOKEN_EXPIRATION_MINUTES = 15;
  private static final Integer REFRESH_TOKEN_EXPIRATION_DAYS = 7;
  private static final Integer REFRESH_TOKEN_REUSE_GRACE_SECONDS = 30;

  @BeforeEach
  void setUp() {
    JwtConfig jwtConfig =
        new JwtConfig(
            SECRET,
            ACCESS_TOKEN_EXPIRATION_MINUTES,
            REFRESH_TOKEN_EXPIRATION_DAYS,
            REFRESH_TOKEN_REUSE_GRACE_SECONDS);
    jwtTokenProvider = new JwtTokenProvider(jwtConfig);
  }

  @Nested
  @DisplayName("#generateAccessToken")
  class GenerateAccessToken {

    @Test
    @DisplayName("正常系: アクセストークンが生成されること")
    void generateAccessToken_success() {
      when(principal.getUsername()).thenReturn("testuser1");
      when(principal.getAccountNo()).thenReturn(1L);
      doReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")))
          .when(principal)
          .getAuthorities();

      String token = jwtTokenProvider.generateAccessToken(principal);

      assertNotNull(token);
      assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("正常系: 生成されたトークンからアカウントIDが取得できること")
    void generateAccessToken_containsAccountId() {
      when(principal.getUsername()).thenReturn("testuser1");
      when(principal.getAccountNo()).thenReturn(1L);
      doReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")))
          .when(principal)
          .getAuthorities();

      String token = jwtTokenProvider.generateAccessToken(principal);
      String accountId = jwtTokenProvider.getAccountIdFromToken(token);

      assertEquals("testuser1", accountId);
    }
  }

  @Nested
  @DisplayName("#generateRefreshToken")
  class GenerateRefreshToken {

    @Test
    @DisplayName("正常系: リフレッシュトークンが生成されること")
    void generateRefreshToken_success() {
      String token = jwtTokenProvider.generateRefreshToken();

      assertNotNull(token);
      assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("正常系: 生成されるトークンが毎回異なること")
    void generateRefreshToken_unique() {
      String token1 = jwtTokenProvider.generateRefreshToken();
      String token2 = jwtTokenProvider.generateRefreshToken();

      assertNotEquals(token1, token2);
    }
  }

  @Nested
  @DisplayName("#validateAccessToken")
  class ValidateAccessToken {

    @Test
    @DisplayName("正常系: 有効なトークンのクレームが取得できること")
    void validateAccessToken_success() {
      when(principal.getUsername()).thenReturn("testuser1");
      when(principal.getAccountNo()).thenReturn(1L);
      doReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")))
          .when(principal)
          .getAuthorities();

      String token = jwtTokenProvider.generateAccessToken(principal);
      Claims claims = jwtTokenProvider.validateAccessToken(token);

      assertEquals("testuser1", claims.getSubject());
      assertEquals("web-gallery", claims.getIssuer());
      assertEquals(1, claims.get("accountNo", Integer.class));
      // 氏名等のPIIはクレームに含めない
      assertNull(claims.get("accountName", String.class));
      assertEquals("ROLE_USER", claims.get("role", String.class));
    }

    @Test
    @DisplayName("異常系: 不正なトークンの場合は例外がスローされること")
    void validateAccessToken_invalidToken() {
      assertThrows(
          JwtException.class,
          () -> {
            jwtTokenProvider.validateAccessToken("invalid-token");
          });
    }
  }

  @Nested
  @DisplayName("#isTokenValid")
  class IsTokenValid {

    @Test
    @DisplayName("正常系: 有効なトークンの場合はtrueを返すこと")
    void isTokenValid_validToken() {
      when(principal.getUsername()).thenReturn("testuser1");
      when(principal.getAccountNo()).thenReturn(1L);
      doReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")))
          .when(principal)
          .getAuthorities();

      String token = jwtTokenProvider.generateAccessToken(principal);

      assertTrue(jwtTokenProvider.isTokenValid(token));
    }

    @Test
    @DisplayName("異常系: 不正なトークンの場合はfalseを返すこと")
    void isTokenValid_invalidToken() {
      assertFalse(jwtTokenProvider.isTokenValid("invalid-token"));
    }

    @Test
    @DisplayName("異常系: nullの場合はfalseを返すこと")
    void isTokenValid_nullToken() {
      assertFalse(jwtTokenProvider.isTokenValid(null));
    }
  }
}
