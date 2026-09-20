package com.web.gallery.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.web.gallery.AccountPrincipal;
import com.web.gallery.config.JwtConfig;
import com.web.gallery.config.LoginConfig;
import com.web.gallery.constant.Consts;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.account.IsAdminLocked;
import com.web.gallery.domain.account.LoginFailureCount;
import com.web.gallery.domain.account.Password;
import com.web.gallery.domain.auth.RefreshTokenValue;
import com.web.gallery.domain.common.ExpiresAt;
import com.web.gallery.domain.common.IpAddress;
import com.web.gallery.domain.common.IsDeleted;
import com.web.gallery.domain.common.IsRevoked;
import com.web.gallery.domain.common.TokenHash;
import com.web.gallery.domain.common.UpdatedAt;
import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.exception.InvalidRefreshTokenException;
import com.web.gallery.helper.JwtTokenProvider;
import com.web.gallery.model.AccountModel;
import com.web.gallery.model.AuthTokenModel;
import com.web.gallery.model.RefreshTokenModel;
import com.web.gallery.repository.impl.AccountRepositoryImpl;
import com.web.gallery.repository.impl.RefreshTokenRepositoryImpl;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

  @InjectMocks private AuthServiceImpl authServiceImpl;

  @Mock private AuthenticationManager authenticationManager;

  @Mock private JwtTokenProvider jwtTokenProvider;

  @Mock private JwtConfig jwtConfig;

  @Mock private LoginConfig loginConfig;

  @Mock private RefreshTokenRepositoryImpl refreshTokenRepositoryImpl;

  @Mock private AccountRepositoryImpl accountRepositoryImpl;

  @Mock private Clock clock;

  @BeforeEach
  void setUpClock() {
    lenient().when(clock.instant()).thenReturn(Instant.now());
    lenient().when(clock.getZone()).thenReturn(Consts.JST);
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class login {

    @Test
    @Order(1)
    @DisplayName("正常系：ログインに成功し、トークンが返されること")
    void login_success() {
      String accountId = "testuser1";
      String password = "password1";

      AccountPrincipal principal = mock(AccountPrincipal.class);
      doReturn(1L).when(principal).getAccountNo();

      Authentication authentication = mock(Authentication.class);
      doReturn(principal).when(authentication).getPrincipal();
      doReturn(authentication)
          .when(authenticationManager)
          .authenticate(any(UsernamePasswordAuthenticationToken.class));

      doReturn("access-token").when(jwtTokenProvider).generateAccessToken(principal);
      doReturn("refresh-token").when(jwtTokenProvider).generateRefreshToken();
      doReturn(7).when(jwtConfig).getRefreshTokenExpirationDays();
      doReturn(15).when(jwtConfig).getAccessTokenExpirationMinutes();

      AuthTokenModel result =
          authServiceImpl.login(
              new AccountId(accountId), new Password(password), new IpAddress("127.0.0.1"));

      assertNotNull(result);
      assertEquals("access-token", result.getAccessToken().value());
      assertEquals("refresh-token", result.getRefreshToken().value());
      assertEquals(900L, result.getExpiresIn().value());

      verify(refreshTokenRepositoryImpl).revokeAllByAccountNo(new AccountNo(1L));
      ArgumentCaptor<RefreshTokenModel> refreshTokenModelCaptor =
          ArgumentCaptor.forClass(RefreshTokenModel.class);
      verify(refreshTokenRepositoryImpl).save(refreshTokenModelCaptor.capture());
      assertEquals(
          OffsetDateTime.now(clock).plusDays(7),
          refreshTokenModelCaptor.getValue().getExpiresAt().value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：ログイン失敗回数が未設定（null）のアカウントはロック状態と判定されず、ログインに成功すること")
    void login_success_loginFailureCountNull() {
      String accountId = "testuser1";
      String password = "password1";

      AccountModel accountModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .updatedAt(new UpdatedAt(OffsetDateTime.now(clock)))
              .build();
      doReturn(accountModel).when(accountRepositoryImpl).getByAccountId(new AccountId(accountId));

      AccountPrincipal principal = mock(AccountPrincipal.class);
      doReturn(1L).when(principal).getAccountNo();

      Authentication authentication = mock(Authentication.class);
      doReturn(principal).when(authentication).getPrincipal();
      doReturn(authentication)
          .when(authenticationManager)
          .authenticate(any(UsernamePasswordAuthenticationToken.class));

      doReturn("access-token").when(jwtTokenProvider).generateAccessToken(principal);
      doReturn("refresh-token").when(jwtTokenProvider).generateRefreshToken();
      doReturn(7).when(jwtConfig).getRefreshTokenExpirationDays();
      doReturn(15).when(jwtConfig).getAccessTokenExpirationMinutes();

      AuthTokenModel result =
          authServiceImpl.login(
              new AccountId(accountId), new Password(password), new IpAddress("127.0.0.1"));

      assertNotNull(result);
      assertEquals("access-token", result.getAccessToken().value());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：パスワードが間違っている場合は例外がスローされること")
    void login_badCredentials() {
      doThrow(new BadCredentialsException("Bad credentials"))
          .when(authenticationManager)
          .authenticate(any(UsernamePasswordAuthenticationToken.class));

      assertThrows(
          BadCredentialsException.class,
          () -> {
            authServiceImpl.login(
                new AccountId("testuser1"),
                new Password("wrongpassword"),
                new IpAddress("127.0.0.1"));
          });
    }

    @Test
    @Order(4)
    @DisplayName("異常系：アカウントがロックされている場合は例外がスローされること")
    void login_accountLocked() {
      doThrow(new LockedException("Account is locked"))
          .when(authenticationManager)
          .authenticate(any(UsernamePasswordAuthenticationToken.class));

      assertThrows(
          LockedException.class,
          () -> {
            authServiceImpl.login(
                new AccountId("testuser1"), new Password("password1"), new IpAddress("127.0.0.1"));
          });
    }

    @Test
    @Order(5)
    @DisplayName("異常系：ロック中かつ最終更新から自動解除時間が経過していない場合は例外がスローされ、認証は行われないこと")
    void login_accountLocked_within_lockDuration() {
      doReturn(3).when(loginConfig).getFailCount();
      doReturn(30).when(loginConfig).getLockDurationMinutes();

      AccountModel lockedModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .loginFailureCount(new LoginFailureCount(3))
              .updatedAt(new UpdatedAt(OffsetDateTime.now(clock).minusMinutes(5)))
              .build();
      doReturn(lockedModel).when(accountRepositoryImpl).getByAccountId(new AccountId("testuser1"));

      assertThrows(
          LockedException.class,
          () -> {
            authServiceImpl.login(
                new AccountId("testuser1"), new Password("password1"), new IpAddress("127.0.0.1"));
          });

      verify(authenticationManager, never())
          .authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @Order(6)
    @DisplayName("正常系：ロック中でも最終更新から自動解除時間が経過していればロックが解除され、ログインに成功すること")
    void login_accountLocked_after_lockDuration_autoReleased() throws Exception {
      doReturn(3).when(loginConfig).getFailCount();
      doReturn(30).when(loginConfig).getLockDurationMinutes();

      AccountModel lockedModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .loginFailureCount(new LoginFailureCount(3))
              .updatedAt(new UpdatedAt(OffsetDateTime.now(clock).minusMinutes(31)))
              .build();
      doReturn(lockedModel).when(accountRepositoryImpl).getByAccountId(new AccountId("testuser1"));

      AccountPrincipal principal = mock(AccountPrincipal.class);
      doReturn(1L).when(principal).getAccountNo();
      Authentication authentication = mock(Authentication.class);
      doReturn(principal).when(authentication).getPrincipal();
      doReturn(authentication)
          .when(authenticationManager)
          .authenticate(any(UsernamePasswordAuthenticationToken.class));
      doReturn("access-token").when(jwtTokenProvider).generateAccessToken(principal);
      doReturn("refresh-token").when(jwtTokenProvider).generateRefreshToken();
      doReturn(7).when(jwtConfig).getRefreshTokenExpirationDays();
      doReturn(15).when(jwtConfig).getAccessTokenExpirationMinutes();

      AuthTokenModel result =
          authServiceImpl.login(
              new AccountId("testuser1"), new Password("password1"), new IpAddress("127.0.0.1"));

      assertNotNull(result);
      ArgumentCaptor<AccountModel> unlockCaptor = ArgumentCaptor.forClass(AccountModel.class);
      verify(accountRepositoryImpl).updateLoginFailureCount(unlockCaptor.capture());
      assertEquals(0, unlockCaptor.getValue().getLoginFailureCount().value());
    }

    @Test
    @Order(7)
    @DisplayName("正常系：ロック解除の更新に失敗してもログイン処理は継続し、ログインに成功すること")
    void login_accountLocked_after_lockDuration_releaseLockFails_loginStillSucceeds()
        throws Exception {
      doReturn(3).when(loginConfig).getFailCount();
      doReturn(30).when(loginConfig).getLockDurationMinutes();

      AccountModel lockedModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .loginFailureCount(new LoginFailureCount(3))
              .updatedAt(new UpdatedAt(OffsetDateTime.now(clock).minusMinutes(31)))
              .build();
      doReturn(lockedModel).when(accountRepositoryImpl).getByAccountId(new AccountId("testuser1"));
      doThrow(ErrorEnum.FAIL_TO_UPDATE_ACCOUNT.toException())
          .when(accountRepositoryImpl)
          .updateLoginFailureCount(any(AccountModel.class));

      AccountPrincipal principal = mock(AccountPrincipal.class);
      doReturn(1L).when(principal).getAccountNo();
      Authentication authentication = mock(Authentication.class);
      doReturn(principal).when(authentication).getPrincipal();
      doReturn(authentication)
          .when(authenticationManager)
          .authenticate(any(UsernamePasswordAuthenticationToken.class));
      doReturn("access-token").when(jwtTokenProvider).generateAccessToken(principal);
      doReturn("refresh-token").when(jwtTokenProvider).generateRefreshToken();
      doReturn(7).when(jwtConfig).getRefreshTokenExpirationDays();
      doReturn(15).when(jwtConfig).getAccessTokenExpirationMinutes();

      AuthTokenModel result =
          authServiceImpl.login(
              new AccountId("testuser1"), new Password("password1"), new IpAddress("127.0.0.1"));

      assertNotNull(result);
      assertEquals("access-token", result.getAccessToken().value());
      verify(accountRepositoryImpl).updateLoginFailureCount(any(AccountModel.class));
    }

    @Test
    @Order(8)
    @DisplayName("異常系：管理者ロックされている場合は、最終更新から自動解除時間が経過していても解除されず例外がスローされること")
    void login_adminLocked_notAutoReleased() throws Exception {
      AccountModel adminLockedModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .loginFailureCount(new LoginFailureCount(0))
              .isAdminLocked(new IsAdminLocked(true))
              .updatedAt(new UpdatedAt(OffsetDateTime.now(clock).minusMinutes(999)))
              .build();
      doReturn(adminLockedModel)
          .when(accountRepositoryImpl)
          .getByAccountId(new AccountId("testuser1"));

      assertThrows(
          LockedException.class,
          () -> {
            authServiceImpl.login(
                new AccountId("testuser1"), new Password("password1"), new IpAddress("127.0.0.1"));
          });

      verify(authenticationManager, never())
          .authenticate(any(UsernamePasswordAuthenticationToken.class));
      verify(accountRepositoryImpl, never()).updateLoginFailureCount(any(AccountModel.class));
    }

    @Test
    @Order(9)
    @DisplayName("異常系：ロック中で最終更新日時が未設定の場合は自動解除できず例外がスローされること")
    void login_locked_updatedAtNull_notAutoReleased() {
      doReturn(3).when(loginConfig).getFailCount();

      AccountModel lockedModel =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .loginFailureCount(new LoginFailureCount(3))
              .build();
      doReturn(lockedModel).when(accountRepositoryImpl).getByAccountId(new AccountId("testuser1"));

      assertThrows(
          LockedException.class,
          () -> {
            authServiceImpl.login(
                new AccountId("testuser1"), new Password("password1"), new IpAddress("127.0.0.1"));
          });

      verify(authenticationManager, never())
          .authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class refresh {

    @Test
    @Order(1)
    @DisplayName("正常系：リフレッシュトークンが有効な場合、新しいアクセストークンとリフレッシュトークンが返され、旧トークンが無効化されること")
    void refresh_success() {
      String refreshToken = "valid-refresh-token";
      RefreshTokenModel storedToken =
          RefreshTokenModel.builder()
              .accountNo(new AccountNo(1L))
              .tokenHash(new TokenHash("hashed-token"))
              .expiresAt(new ExpiresAt(OffsetDateTime.now().plusDays(7)))
              .isRevoked(new IsRevoked(false))
              .build();

      doReturn(storedToken)
          .when(refreshTokenRepositoryImpl)
          .findByTokenHashForUpdate(any(TokenHash.class));

      AccountModel account =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("testuser1"))
              .loginFailureCount(new LoginFailureCount(0))
              .isDeleted(new IsDeleted(false))
              .authorityKbn(AuthorityEnum.MINI)
              .build();
      doReturn(account).when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
      doReturn(3).when(loginConfig).getFailCount();

      doReturn("new-access-token")
          .when(jwtTokenProvider)
          .generateAccessToken(any(AccountPrincipal.class));
      doReturn("new-refresh-token").when(jwtTokenProvider).generateRefreshToken();
      doReturn(15).when(jwtConfig).getAccessTokenExpirationMinutes();
      doReturn(7).when(jwtConfig).getRefreshTokenExpirationDays();

      AuthTokenModel result = authServiceImpl.refresh(new RefreshTokenValue(refreshToken));

      assertNotNull(result);
      assertEquals("new-access-token", result.getAccessToken().value());
      assertEquals("new-refresh-token", result.getRefreshToken().value());
      assertEquals(900L, result.getExpiresIn().value());

      verify(refreshTokenRepositoryImpl).revokeByTokenHash(any(TokenHash.class));
      verify(refreshTokenRepositoryImpl, never()).revokeAllByAccountNo(any(AccountNo.class));

      ArgumentCaptor<RefreshTokenModel> refreshTokenModelCaptor =
          ArgumentCaptor.forClass(RefreshTokenModel.class);
      verify(refreshTokenRepositoryImpl).save(refreshTokenModelCaptor.capture());
      assertEquals(new AccountNo(1L), refreshTokenModelCaptor.getValue().getAccountNo());
      assertEquals(
          OffsetDateTime.now(clock).plusDays(7),
          refreshTokenModelCaptor.getValue().getExpiresAt().value());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：アカウントがロックされている場合は例外がスローされること")
    void refresh_accountLocked() {
      String refreshToken = "valid-refresh-token";
      RefreshTokenModel storedToken =
          RefreshTokenModel.builder()
              .accountNo(new AccountNo(1L))
              .tokenHash(new TokenHash("hashed-token"))
              .expiresAt(new ExpiresAt(OffsetDateTime.now().plusDays(7)))
              .isRevoked(new IsRevoked(false))
              .build();

      doReturn(storedToken)
          .when(refreshTokenRepositoryImpl)
          .findByTokenHashForUpdate(any(TokenHash.class));

      AccountModel account =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("testuser1"))
              .loginFailureCount(new LoginFailureCount(3))
              .isDeleted(new IsDeleted(false))
              .authorityKbn(AuthorityEnum.MINI)
              .build();
      doReturn(account).when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
      doReturn(3).when(loginConfig).getFailCount();

      assertThrows(
          LockedException.class,
          () -> {
            authServiceImpl.refresh(new RefreshTokenValue(refreshToken));
          });
    }

    @Test
    @Order(3)
    @DisplayName("正常系：ロック中でも最終更新から自動解除時間が経過していればロックが解除され、リフレッシュに成功すること")
    void refresh_accountLocked_after_lockDuration_autoReleased() throws Exception {
      doReturn(30).when(loginConfig).getLockDurationMinutes();

      RefreshTokenModel storedToken =
          RefreshTokenModel.builder()
              .accountNo(new AccountNo(1L))
              .tokenHash(new TokenHash("hashed-token"))
              .expiresAt(new ExpiresAt(OffsetDateTime.now().plusDays(7)))
              .isRevoked(new IsRevoked(false))
              .build();
      doReturn(storedToken)
          .when(refreshTokenRepositoryImpl)
          .findByTokenHashForUpdate(any(TokenHash.class));

      AccountModel account =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("testuser1"))
              .loginFailureCount(new LoginFailureCount(3))
              .isDeleted(new IsDeleted(false))
              .authorityKbn(AuthorityEnum.MINI)
              .updatedAt(new UpdatedAt(OffsetDateTime.now(clock).minusMinutes(31)))
              .build();
      doReturn(account).when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
      doReturn(3).when(loginConfig).getFailCount();

      doReturn("new-access-token")
          .when(jwtTokenProvider)
          .generateAccessToken(any(AccountPrincipal.class));
      doReturn("new-refresh-token").when(jwtTokenProvider).generateRefreshToken();
      doReturn(15).when(jwtConfig).getAccessTokenExpirationMinutes();
      doReturn(7).when(jwtConfig).getRefreshTokenExpirationDays();

      AuthTokenModel result = authServiceImpl.refresh(new RefreshTokenValue("valid-refresh-token"));

      assertNotNull(result);
      ArgumentCaptor<AccountModel> unlockCaptor = ArgumentCaptor.forClass(AccountModel.class);
      verify(accountRepositoryImpl).updateLoginFailureCount(unlockCaptor.capture());
      assertEquals(0, unlockCaptor.getValue().getLoginFailureCount().value());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：アカウントが無効化（削除済み）されている場合は例外がスローされること")
    void refresh_accountDisabled() {
      String refreshToken = "valid-refresh-token";
      RefreshTokenModel storedToken =
          RefreshTokenModel.builder()
              .accountNo(new AccountNo(1L))
              .tokenHash(new TokenHash("hashed-token"))
              .expiresAt(new ExpiresAt(OffsetDateTime.now().plusDays(7)))
              .isRevoked(new IsRevoked(false))
              .build();

      doReturn(storedToken)
          .when(refreshTokenRepositoryImpl)
          .findByTokenHashForUpdate(any(TokenHash.class));

      AccountModel account =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("testuser1"))
              .loginFailureCount(new LoginFailureCount(0))
              .isDeleted(new IsDeleted(true))
              .authorityKbn(AuthorityEnum.MINI)
              .build();
      doReturn(account).when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
      doReturn(3).when(loginConfig).getFailCount();

      assertThrows(
          InvalidRefreshTokenException.class,
          () -> {
            authServiceImpl.refresh(new RefreshTokenValue(refreshToken));
          });
    }

    @Test
    @Order(5)
    @DisplayName("異常系：無効化済み（ローテーション済み）トークンが再利用された場合、盗用とみなし該当アカウントの全トークンを失効させたうえで例外がスローされること")
    void refresh_revokedToken() {
      RefreshTokenModel storedToken =
          RefreshTokenModel.builder()
              .accountNo(new AccountNo(1L))
              .tokenHash(new TokenHash("hashed-token"))
              .expiresAt(new ExpiresAt(OffsetDateTime.now().plusDays(7)))
              .isRevoked(new IsRevoked(true))
              .build();

      doReturn(storedToken)
          .when(refreshTokenRepositoryImpl)
          .findByTokenHashForUpdate(any(TokenHash.class));

      assertThrows(
          InvalidRefreshTokenException.class,
          () -> {
            authServiceImpl.refresh(new RefreshTokenValue("revoked-token"));
          });

      verify(refreshTokenRepositoryImpl).revokeAllByAccountNo(new AccountNo(1L));
      verify(refreshTokenRepositoryImpl, never()).save(any(RefreshTokenModel.class));
    }

    @Test
    @Order(6)
    @DisplayName("正常系：ローテーション直後の猶予期間内に無効化済みトークンが再送された場合は、正常系のリトライとみなし全トークンは失効させずに拒否のみ行うこと")
    void refresh_revokedToken_withinGracePeriod() {
      doReturn(30).when(jwtConfig).getRefreshTokenReuseGraceSeconds();

      RefreshTokenModel storedToken =
          RefreshTokenModel.builder()
              .accountNo(new AccountNo(1L))
              .tokenHash(new TokenHash("hashed-token"))
              .expiresAt(new ExpiresAt(OffsetDateTime.now().plusDays(7)))
              .isRevoked(new IsRevoked(true))
              .updatedAt(new UpdatedAt(OffsetDateTime.now(clock).minusSeconds(5)))
              .build();

      doReturn(storedToken)
          .when(refreshTokenRepositoryImpl)
          .findByTokenHashForUpdate(any(TokenHash.class));

      assertThrows(
          InvalidRefreshTokenException.class,
          () -> {
            authServiceImpl.refresh(new RefreshTokenValue("rotated-token"));
          });

      verify(refreshTokenRepositoryImpl, never()).revokeAllByAccountNo(any(AccountNo.class));
      verify(refreshTokenRepositoryImpl, never()).save(any(RefreshTokenModel.class));
    }

    @Test
    @Order(7)
    @DisplayName("異常系：リフレッシュトークンの有効期限が切れている場合は例外がスローされること")
    void refresh_expiredToken() {
      RefreshTokenModel storedToken =
          RefreshTokenModel.builder()
              .accountNo(new AccountNo(1L))
              .tokenHash(new TokenHash("hashed-token"))
              .expiresAt(new ExpiresAt(OffsetDateTime.now().minusDays(1)))
              .isRevoked(new IsRevoked(false))
              .build();

      doReturn(storedToken)
          .when(refreshTokenRepositoryImpl)
          .findByTokenHashForUpdate(any(TokenHash.class));

      assertThrows(
          InvalidRefreshTokenException.class,
          () -> {
            authServiceImpl.refresh(new RefreshTokenValue("expired-token"));
          });
    }

    @Test
    @Order(8)
    @DisplayName("異常系：リフレッシュトークンが存在しない場合は例外がスローされること")
    void refresh_tokenNotFound() {
      doReturn(null)
          .when(refreshTokenRepositoryImpl)
          .findByTokenHashForUpdate(any(TokenHash.class));

      assertThrows(
          InvalidRefreshTokenException.class,
          () -> {
            authServiceImpl.refresh(new RefreshTokenValue("nonexistent-token"));
          });
    }

    @Test
    @Order(9)
    @DisplayName("異常系：トークンに紐づくアカウントが既に削除されている場合は例外がスローされること")
    void refresh_accountNotFound() {
      RefreshTokenModel storedToken =
          RefreshTokenModel.builder()
              .accountNo(new AccountNo(1L))
              .tokenHash(new TokenHash("hashed-token"))
              .expiresAt(new ExpiresAt(OffsetDateTime.now().plusDays(7)))
              .isRevoked(new IsRevoked(false))
              .build();

      doReturn(storedToken)
          .when(refreshTokenRepositoryImpl)
          .findByTokenHashForUpdate(any(TokenHash.class));
      doReturn(null).when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));

      assertThrows(
          InvalidRefreshTokenException.class,
          () -> {
            authServiceImpl.refresh(new RefreshTokenValue("valid-refresh-token"));
          });
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class logout {

    @Test
    @Order(1)
    @DisplayName("正常系：リフレッシュトークンが無効化されること")
    void logout_success() {
      authServiceImpl.logout(new RefreshTokenValue("refresh-token"));

      verify(refreshTokenRepositoryImpl).revokeByTokenHash(any(TokenHash.class));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class purgeExpiredRefreshTokens {

    @Test
    @Order(1)
    @DisplayName("正常系：有効期限切れのリフレッシュトークンが削除されること")
    void purgeExpiredRefreshTokens_success() {
      authServiceImpl.purgeExpiredRefreshTokens();

      verify(refreshTokenRepositoryImpl).deleteExpired();
    }
  }
}
