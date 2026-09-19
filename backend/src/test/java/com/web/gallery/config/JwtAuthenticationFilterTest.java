package com.web.gallery.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.web.gallery.AccountPrincipal;
import com.web.gallery.helper.AuthenticatedUserCache;
import com.web.gallery.helper.JwtTokenProvider;
import com.web.gallery.service.impl.AccountServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private AccountServiceImpl accountServiceImpl;
  @Mock private AuthenticatedUserCache authenticatedUserCache;

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private FilterChain filterChain;
  @Mock private AccountPrincipal accountPrincipal;

  @BeforeEach
  void setUp() {
    jwtAuthenticationFilter =
        new JwtAuthenticationFilter(jwtTokenProvider, accountServiceImpl, authenticatedUserCache);
  }

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @SuppressWarnings("unchecked")
  private void stubCacheToInvokeLoader() {
    doAnswer(
            invocation -> {
              Supplier<AccountPrincipal> loader = invocation.getArgument(1);
              return loader.get();
            })
        .when(authenticatedUserCache)
        .get(anyString(), any(Supplier.class));
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class doFilterInternal {
    @Test
    @Order(1)
    @DisplayName("正常系：Authorizationヘッダがない場合、認証処理を行わずフィルタチェーンへ進むこと")
    void noAuthorizationHeader() throws Exception {
      doReturn(null).when(request).getHeader("Authorization");

      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      verify(filterChain, times(1)).doFilter(request, response);
      verifyNoInteractions(jwtTokenProvider);
      assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：AuthorizationヘッダがBearer形式でない場合、認証処理を行わずフィルタチェーンへ進むこと")
    void notBearerHeader() throws Exception {
      doReturn("Basic xxxxx").when(request).getHeader("Authorization");

      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      verify(filterChain, times(1)).doFilter(request, response);
      verifyNoInteractions(jwtTokenProvider);
      assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：トークンが無効な場合、認証情報を設定せずフィルタチェーンへ進むこと")
    void invalidToken() throws Exception {
      doReturn("Bearer invalid-token").when(request).getHeader("Authorization");
      doReturn(false).when(jwtTokenProvider).isTokenValid("invalid-token");

      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      verify(filterChain, times(1)).doFilter(request, response);
      assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @Order(4)
    @DisplayName("正常系：有効なトークンかつ全ステータス正常な場合、認証情報を設定すること")
    void validTokenAndEnabledUser() throws Exception {
      doReturn("Bearer valid-token").when(request).getHeader("Authorization");
      doReturn(true).when(jwtTokenProvider).isTokenValid("valid-token");
      doReturn("testuser01").when(jwtTokenProvider).getAccountIdFromToken("valid-token");
      stubCacheToInvokeLoader();
      doReturn(accountPrincipal).when(accountServiceImpl).loadUserByUsername("testuser01");
      doReturn(true).when(accountPrincipal).isEnabled();
      doReturn(true).when(accountPrincipal).isAccountNonLocked();
      doReturn(true).when(accountPrincipal).isAccountNonExpired();
      doReturn(true).when(accountPrincipal).isCredentialsNonExpired();

      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      verify(filterChain, times(1)).doFilter(request, response);
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      assertNotNull(authentication);
      assertEquals(accountPrincipal, authentication.getPrincipal());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：ユーザーが無効(enabled=false)な場合、認証情報を設定しないこと")
    void disabledUser() throws Exception {
      doReturn("Bearer valid-token").when(request).getHeader("Authorization");
      doReturn(true).when(jwtTokenProvider).isTokenValid("valid-token");
      doReturn("testuser01").when(jwtTokenProvider).getAccountIdFromToken("valid-token");
      stubCacheToInvokeLoader();
      doReturn(accountPrincipal).when(accountServiceImpl).loadUserByUsername("testuser01");
      doReturn(false).when(accountPrincipal).isEnabled();

      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      verify(filterChain, times(1)).doFilter(request, response);
      assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @Order(6)
    @DisplayName("異常系：アカウントがロックされている場合、認証情報を設定しないこと")
    void accountLocked() throws Exception {
      doReturn("Bearer valid-token").when(request).getHeader("Authorization");
      doReturn(true).when(jwtTokenProvider).isTokenValid("valid-token");
      doReturn("testuser01").when(jwtTokenProvider).getAccountIdFromToken("valid-token");
      stubCacheToInvokeLoader();
      doReturn(accountPrincipal).when(accountServiceImpl).loadUserByUsername("testuser01");
      doReturn(true).when(accountPrincipal).isEnabled();
      doReturn(false).when(accountPrincipal).isAccountNonLocked();

      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      verify(filterChain, times(1)).doFilter(request, response);
      assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @Order(7)
    @DisplayName("異常系：アカウントの有効期限が切れている場合、認証情報を設定しないこと")
    void accountExpired() throws Exception {
      doReturn("Bearer valid-token").when(request).getHeader("Authorization");
      doReturn(true).when(jwtTokenProvider).isTokenValid("valid-token");
      doReturn("testuser01").when(jwtTokenProvider).getAccountIdFromToken("valid-token");
      stubCacheToInvokeLoader();
      doReturn(accountPrincipal).when(accountServiceImpl).loadUserByUsername("testuser01");
      doReturn(true).when(accountPrincipal).isEnabled();
      doReturn(true).when(accountPrincipal).isAccountNonLocked();
      doReturn(false).when(accountPrincipal).isAccountNonExpired();

      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      verify(filterChain, times(1)).doFilter(request, response);
      assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @Order(8)
    @DisplayName("異常系：資格情報の有効期限が切れている場合、認証情報を設定しないこと")
    void credentialsExpired() throws Exception {
      doReturn("Bearer valid-token").when(request).getHeader("Authorization");
      doReturn(true).when(jwtTokenProvider).isTokenValid("valid-token");
      doReturn("testuser01").when(jwtTokenProvider).getAccountIdFromToken("valid-token");
      stubCacheToInvokeLoader();
      doReturn(accountPrincipal).when(accountServiceImpl).loadUserByUsername("testuser01");
      doReturn(true).when(accountPrincipal).isEnabled();
      doReturn(true).when(accountPrincipal).isAccountNonLocked();
      doReturn(true).when(accountPrincipal).isAccountNonExpired();
      doReturn(false).when(accountPrincipal).isCredentialsNonExpired();

      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      verify(filterChain, times(1)).doFilter(request, response);
      assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @Order(9)
    @DisplayName("異常系：アカウントIDに該当するユーザーが存在しない場合、認証情報を設定せず処理を継続すること")
    void usernameNotFound() throws Exception {
      doReturn("Bearer valid-token").when(request).getHeader("Authorization");
      doReturn(true).when(jwtTokenProvider).isTokenValid("valid-token");
      doReturn("deleted-user").when(jwtTokenProvider).getAccountIdFromToken("valid-token");
      doThrow(new UsernameNotFoundException("not found"))
          .when(authenticatedUserCache)
          .get(eq("deleted-user"), any());

      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      verify(filterChain, times(1)).doFilter(request, response);
      assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @Order(10)
    @DisplayName("正常系：既にSecurityContextに認証情報が設定されている場合、再設定せずフィルタチェーンへ進むこと")
    void alreadyAuthenticated() throws Exception {
      doReturn("Bearer valid-token").when(request).getHeader("Authorization");
      doReturn(true).when(jwtTokenProvider).isTokenValid("valid-token");
      doReturn("testuser01").when(jwtTokenProvider).getAccountIdFromToken("valid-token");

      Authentication existing =
          new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
              "existing-user", null);
      SecurityContextHolder.getContext().setAuthentication(existing);

      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      verify(filterChain, times(1)).doFilter(request, response);
      verifyNoInteractions(authenticatedUserCache);
      assertEquals(existing, SecurityContextHolder.getContext().getAuthentication());
    }
  }
}
