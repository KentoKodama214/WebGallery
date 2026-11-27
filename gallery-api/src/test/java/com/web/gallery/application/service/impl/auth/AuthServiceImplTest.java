package com.web.gallery.application.service.impl.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.web.gallery.application.AccountPrincipal;
import com.web.gallery.application.config.JwtConfig;
import com.web.gallery.application.config.LoginConfig;
import com.web.gallery.application.config.WebAuthnConfig;
import com.web.gallery.application.helper.JwtTokenProvider;
import com.web.gallery.application.helper.WebAuthnVerifier;
import com.web.gallery.application.model.account.AccountModel;
import com.web.gallery.application.model.auth.AuthTokenModel;
import com.web.gallery.application.model.auth.LoginContextModel;
import com.web.gallery.application.model.auth.PasskeyAssertionModel;
import com.web.gallery.application.model.auth.PasskeyAssertionResultModel;
import com.web.gallery.application.model.auth.PasskeyAttestationModel;
import com.web.gallery.application.model.auth.PasskeyAuthenticationOptionsModel;
import com.web.gallery.application.model.auth.PasskeyCredentialModel;
import com.web.gallery.application.model.auth.PasskeyCredentialModelList;
import com.web.gallery.application.model.auth.PasskeyRegistrationOptionsModel;
import com.web.gallery.application.model.auth.RefreshTokenModel;
import com.web.gallery.application.model.auth.WebAuthnChallengeModel;
import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.enumeration.AuthorityEnum;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.enumeration.LoginMethodEnum;
import com.web.gallery.domain.enumeration.WebAuthnChallengePurposeEnum;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.exception.InvalidRecoveryCodeException;
import com.web.gallery.domain.exception.InvalidRefreshTokenException;
import com.web.gallery.domain.exception.PasskeyVerificationException;
import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountName;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.account.IsAdminLocked;
import com.web.gallery.domain.model.account.LoginFailureCount;
import com.web.gallery.domain.model.auth.AttestedCredentialData;
import com.web.gallery.domain.model.auth.CredentialId;
import com.web.gallery.domain.model.auth.IsBackedUp;
import com.web.gallery.domain.model.auth.IsBackupEligible;
import com.web.gallery.domain.model.auth.IsUvInitialized;
import com.web.gallery.domain.model.auth.PasskeyChallenge;
import com.web.gallery.domain.model.auth.PasskeyName;
import com.web.gallery.domain.model.auth.PasskeyNo;
import com.web.gallery.domain.model.auth.PasskeyResponseJson;
import com.web.gallery.domain.model.auth.PasskeyTransports;
import com.web.gallery.domain.model.auth.PasskeyUserHandle;
import com.web.gallery.domain.model.auth.RecoveryCode;
import com.web.gallery.domain.model.auth.RefreshTokenValue;
import com.web.gallery.domain.model.auth.SignCount;
import com.web.gallery.domain.model.common.ExpiresAt;
import com.web.gallery.domain.model.common.IpAddress;
import com.web.gallery.domain.model.common.IsDeleted;
import com.web.gallery.domain.model.common.IsRevoked;
import com.web.gallery.domain.model.common.TokenHash;
import com.web.gallery.domain.model.common.UpdatedAt;
import com.web.gallery.infrastructure.persistence.repository.account.AccountRepositoryImpl;
import com.web.gallery.infrastructure.persistence.repository.auth.PasskeyCredentialRepositoryImpl;
import com.web.gallery.infrastructure.persistence.repository.auth.PasskeyUserHandleRepositoryImpl;
import com.web.gallery.infrastructure.persistence.repository.auth.RecoveryCodeRepositoryImpl;
import com.web.gallery.infrastructure.persistence.repository.auth.RefreshTokenRepositoryImpl;
import com.web.gallery.infrastructure.persistence.repository.auth.WebAuthnChallengeRepositoryImpl;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

  @InjectMocks private AuthServiceImpl authServiceImpl;

  @Mock private JwtTokenProvider jwtTokenProvider;

  @Mock private JwtConfig jwtConfig;

  @Mock private LoginConfig loginConfig;

  @Mock private RefreshTokenRepositoryImpl refreshTokenRepositoryImpl;

  @Mock private AccountRepositoryImpl accountRepositoryImpl;

  @Mock private PasskeyCredentialRepositoryImpl passkeyCredentialRepositoryImpl;

  @Mock private PasskeyUserHandleRepositoryImpl passkeyUserHandleRepositoryImpl;

  @Mock private WebAuthnChallengeRepositoryImpl webAuthnChallengeRepositoryImpl;

  @Mock private RecoveryCodeRepositoryImpl recoveryCodeRepositoryImpl;

  @Mock private WebAuthnVerifier webAuthnVerifier;

  @Mock private WebAuthnConfig webAuthnConfig;

  @Mock private ApplicationEventPublisher applicationEventPublisher;

  @Mock private Clock clock;

  @BeforeEach
  void setUpClock() {
    lenient().when(clock.instant()).thenReturn(Instant.now());
    lenient().when(clock.getZone()).thenReturn(Consts.JST);
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

  @Nested
  @Order(5)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class createPasskeyAuthenticationOptions {
    @Test
    @Order(1)
    @DisplayName("正常系：認証用チャレンジをアカウント未確定（0）で保存し、RP ID・タイムアウトとともに返すこと")
    void createPasskeyAuthenticationOptions_success() {
      PasskeyChallenge challenge = new PasskeyChallenge("challenge-1");
      doReturn(challenge).when(webAuthnVerifier).generateChallenge();
      doReturn(300).when(webAuthnConfig).getChallengeTimeoutSeconds();
      doReturn("localhost").when(webAuthnConfig).getRpId();

      PasskeyAuthenticationOptionsModel actual =
          authServiceImpl.createPasskeyAuthenticationOptions();

      assertEquals(challenge, actual.getChallenge());
      assertEquals("localhost", actual.getRpId().value());
      assertEquals(300_000L, actual.getTimeoutMillis().value());
      // ログインではアカウントを特定しないため、allowCredentialsは空
      assertTrue(actual.getAllowCredentialIds().isEmpty());

      ArgumentCaptor<WebAuthnChallengeModel> captor =
          ArgumentCaptor.forClass(WebAuthnChallengeModel.class);
      verify(webAuthnChallengeRepositoryImpl).save(captor.capture());
      assertEquals(challenge, captor.getValue().getChallenge());
      assertEquals(WebAuthnChallengePurposeEnum.AUTHENTICATION, captor.getValue().getPurposeKbn());
      // 認証前でアカウントが未確定のため、アカウント番号は持たない
      assertNull(captor.getValue().getAccountNo());
      assertEquals(
          OffsetDateTime.now(clock).plusSeconds(300), captor.getValue().getExpiresAt().value());
    }
  }

  @Nested
  @Order(6)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class loginWithPasskey {
    private static final PasskeyResponseJson JSON = new PasskeyResponseJson("{\"id\":\"x\"}");
    private static final IpAddress IP = new IpAddress("127.0.0.1");
    private static final CredentialId CREDENTIAL_ID = new CredentialId("credential-1");
    private static final PasskeyChallenge CHALLENGE = new PasskeyChallenge("challenge-1");
    private static final PasskeyUserHandle USER_HANDLE = new PasskeyUserHandle("handle-1");

    private PasskeyCredentialModel credential;

    @BeforeEach
    void setUp() {
      credential =
          PasskeyCredentialModel.builder()
              .passkeyNo(new PasskeyNo(10L))
              .accountNo(new AccountNo(1L))
              .credentialId(CREDENTIAL_ID)
              .attestedCredentialData(new AttestedCredentialData(new byte[] {1}))
              .signCount(new SignCount(5L))
              .transports(new PasskeyTransports("internal"))
              .isBackupEligible(new IsBackupEligible(true))
              .isBackedUp(new IsBackedUp(false))
              .isUvInitialized(new IsUvInitialized(true))
              .passkeyName(new PasskeyName("MacBook"))
              .build();
    }

    private AccountModel account(int loginFailureCount, boolean adminLocked, boolean deleted) {
      return AccountModel.builder()
          .accountNo(new AccountNo(1L))
          .accountId(new AccountId("testuser1"))
          .accountName(new AccountName("テストユーザー"))
          .authorityKbn(AuthorityEnum.NORMAL)
          .loginFailureCount(new LoginFailureCount(loginFailureCount))
          .isAdminLocked(new IsAdminLocked(adminLocked))
          .isDeleted(new IsDeleted(deleted))
          .updatedAt(new UpdatedAt(OffsetDateTime.now(clock).minusMinutes(5)))
          .build();
    }

    /** 認証レスポンスの読み取り・クレデンシャル・チャレンジ・ユーザーハンドルの引き当てまでを成功させる */
    private void stubAssertionLookup(PasskeyUserHandle responseUserHandle) {
      doReturn(
              PasskeyAssertionModel.builder()
                  .credentialId(CREDENTIAL_ID)
                  .challenge(CHALLENGE)
                  .userHandle(responseUserHandle)
                  .build())
          .when(webAuthnVerifier)
          .readAuthentication(JSON);
      doReturn(credential)
          .when(passkeyCredentialRepositoryImpl)
          .findByCredentialIdForUpdate(CREDENTIAL_ID);
      doReturn(
              WebAuthnChallengeModel.of(
                  CHALLENGE,
                  WebAuthnChallengePurposeEnum.AUTHENTICATION,
                  null,
                  new ExpiresAt(OffsetDateTime.now(clock).plusMinutes(5))))
          .when(webAuthnChallengeRepositoryImpl)
          .consumeForAuthentication(CHALLENGE);
      lenient()
          .doReturn(USER_HANDLE)
          .when(passkeyUserHandleRepositoryImpl)
          .findByAccountNo(new AccountNo(1L));
    }

    private void stubTokenIssuance() {
      doReturn("access-token").when(jwtTokenProvider).generateAccessToken(any());
      doReturn("refresh-token").when(jwtTokenProvider).generateRefreshToken();
      doReturn(7).when(jwtConfig).getRefreshTokenExpirationDays();
      doReturn(15).when(jwtConfig).getAccessTokenExpirationMinutes();
    }

    @Test
    @Order(1)
    @DisplayName("正常系：検証に成功した場合、署名カウンタ等を更新し、認証成功イベントを発行してトークンを返すこと")
    void loginWithPasskey_success() {
      stubAssertionLookup(USER_HANDLE);
      doReturn(account(0, false, false))
          .when(accountRepositoryImpl)
          .getByAccountNo(new AccountNo(1L));
      doReturn(3).when(loginConfig).getFailCount();
      doReturn(
              PasskeyAssertionResultModel.builder()
                  .signCount(new SignCount(6L))
                  .isBackedUp(new IsBackedUp(true))
                  .build())
          .when(webAuthnVerifier)
          .verifyAuthentication(JSON, CHALLENGE, credential);
      stubTokenIssuance();

      AuthTokenModel actual = authServiceImpl.loginWithPasskey(JSON, IP);

      assertEquals("access-token", actual.getAccessToken().value());
      assertEquals("refresh-token", actual.getRefreshToken().value());
      verify(accountRepositoryImpl).lockForLoginAttempt(new AccountId("testuser1"));

      ArgumentCaptor<PasskeyCredentialModel> credentialCaptor =
          ArgumentCaptor.forClass(PasskeyCredentialModel.class);
      verify(passkeyCredentialRepositoryImpl)
          .updateAuthenticationResult(credentialCaptor.capture());
      assertEquals(new SignCount(6L), credentialCaptor.getValue().getSignCount());
      assertEquals(new IsBackedUp(true), credentialCaptor.getValue().getIsBackedUp());
      assertEquals(OffsetDateTime.now(clock), credentialCaptor.getValue().getLastUsedAt().value());

      ArgumentCaptor<AuthenticationSuccessEvent> eventCaptor =
          ArgumentCaptor.forClass(AuthenticationSuccessEvent.class);
      verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
      assertEquals("testuser1", eventCaptor.getValue().getAuthentication().getName());
      assertEquals(
          LoginContextModel.of(IP, LoginMethodEnum.PASSKEY),
          eventCaptor.getValue().getAuthentication().getDetails());

      // 同時セッション制限として既存のリフレッシュトークンを失効させてから新しいトークンを保存する
      verify(refreshTokenRepositoryImpl).revokeAllByAccountNo(new AccountNo(1L));
      verify(refreshTokenRepositoryImpl).save(any(RefreshTokenModel.class));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：認証器がユーザーハンドルを返さない場合も、署名の検証に成功すればログインできること")
    void loginWithPasskey_withoutUserHandle() {
      stubAssertionLookup(null);
      doReturn(account(0, false, false))
          .when(accountRepositoryImpl)
          .getByAccountNo(new AccountNo(1L));
      doReturn(3).when(loginConfig).getFailCount();
      doReturn(
              PasskeyAssertionResultModel.builder()
                  .signCount(new SignCount(6L))
                  .isBackedUp(new IsBackedUp(false))
                  .build())
          .when(webAuthnVerifier)
          .verifyAuthentication(JSON, CHALLENGE, credential);
      stubTokenIssuance();

      assertNotNull(authServiceImpl.loginWithPasskey(JSON, IP));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：失敗回数ロックの解除時間を過ぎている場合、ロックを自動解除してログインできること")
    void loginWithPasskey_lockDurationElapsed() throws Exception {
      stubAssertionLookup(USER_HANDLE);
      AccountModel locked =
          AccountModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("testuser1"))
              .accountName(new AccountName("テストユーザー"))
              .authorityKbn(AuthorityEnum.NORMAL)
              .loginFailureCount(new LoginFailureCount(3))
              .isAdminLocked(new IsAdminLocked(false))
              .isDeleted(new IsDeleted(false))
              .updatedAt(new UpdatedAt(OffsetDateTime.now(clock).minusMinutes(31)))
              .build();
      doReturn(locked).when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
      doReturn(3).when(loginConfig).getFailCount();
      doReturn(30).when(loginConfig).getLockDurationMinutes();
      doReturn(
              PasskeyAssertionResultModel.builder()
                  .signCount(new SignCount(6L))
                  .isBackedUp(new IsBackedUp(false))
                  .build())
          .when(webAuthnVerifier)
          .verifyAuthentication(JSON, CHALLENGE, credential);
      stubTokenIssuance();

      assertNotNull(authServiceImpl.loginWithPasskey(JSON, IP));
      verify(accountRepositoryImpl).updateLoginFailureCount(any(AccountModel.class));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：未登録のクレデンシャルの場合、PasskeyVerificationExceptionをスローし、チャレンジを消費しないこと")
    void loginWithPasskey_unknownCredential() {
      doReturn(
              PasskeyAssertionModel.builder()
                  .credentialId(CREDENTIAL_ID)
                  .challenge(CHALLENGE)
                  .build())
          .when(webAuthnVerifier)
          .readAuthentication(JSON);
      doReturn(null)
          .when(passkeyCredentialRepositoryImpl)
          .findByCredentialIdForUpdate(CREDENTIAL_ID);

      assertThrows(
          PasskeyVerificationException.class, () -> authServiceImpl.loginWithPasskey(JSON, IP));
      verify(webAuthnChallengeRepositoryImpl, never()).consumeForAuthentication(any());
      verify(refreshTokenRepositoryImpl, never()).save(any());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：チャレンジが未発行・使用済み・期限切れの場合、PasskeyVerificationExceptionをスローすること")
    void loginWithPasskey_challengeNotAvailable() {
      doReturn(
              PasskeyAssertionModel.builder()
                  .credentialId(CREDENTIAL_ID)
                  .challenge(CHALLENGE)
                  .build())
          .when(webAuthnVerifier)
          .readAuthentication(JSON);
      doReturn(credential)
          .when(passkeyCredentialRepositoryImpl)
          .findByCredentialIdForUpdate(CREDENTIAL_ID);
      doReturn(null).when(webAuthnChallengeRepositoryImpl).consumeForAuthentication(CHALLENGE);

      assertThrows(
          PasskeyVerificationException.class, () -> authServiceImpl.loginWithPasskey(JSON, IP));
      verify(webAuthnVerifier, never()).verifyAuthentication(any(), any(), any());
      verify(refreshTokenRepositoryImpl, never()).save(any());
    }

    @Test
    @Order(6)
    @DisplayName("異常系：ユーザーハンドルがパスキーの所有者と一致しない場合、PasskeyVerificationExceptionをスローすること")
    void loginWithPasskey_userHandleMismatch() {
      stubAssertionLookup(new PasskeyUserHandle("other-handle"));
      doReturn(account(0, false, false))
          .when(accountRepositoryImpl)
          .getByAccountNo(new AccountNo(1L));

      assertThrows(
          PasskeyVerificationException.class, () -> authServiceImpl.loginWithPasskey(JSON, IP));
      verify(webAuthnVerifier, never()).verifyAuthentication(any(), any(), any());
      verify(refreshTokenRepositoryImpl, never()).save(any());
    }

    @Test
    @Order(7)
    @DisplayName("異常系：ログイン失敗回数の上限に達しロック中の場合、LockedExceptionをスローし署名を検証しないこと")
    void loginWithPasskey_lockedByFailureCount() {
      stubAssertionLookup(USER_HANDLE);
      doReturn(account(3, false, false))
          .when(accountRepositoryImpl)
          .getByAccountNo(new AccountNo(1L));
      doReturn(3).when(loginConfig).getFailCount();
      doReturn(30).when(loginConfig).getLockDurationMinutes();

      assertThrows(LockedException.class, () -> authServiceImpl.loginWithPasskey(JSON, IP));
      verify(webAuthnVerifier, never()).verifyAuthentication(any(), any(), any());
      verify(applicationEventPublisher, never()).publishEvent(any());
      verify(refreshTokenRepositoryImpl, never()).save(any());
    }

    @Test
    @Order(8)
    @DisplayName("異常系：管理者ロック中の場合、LockedExceptionをスローすること")
    void loginWithPasskey_adminLocked() {
      stubAssertionLookup(USER_HANDLE);
      doReturn(account(0, true, false))
          .when(accountRepositoryImpl)
          .getByAccountNo(new AccountNo(1L));
      lenient().doReturn(3).when(loginConfig).getFailCount();

      assertThrows(LockedException.class, () -> authServiceImpl.loginWithPasskey(JSON, IP));
      verify(webAuthnVerifier, never()).verifyAuthentication(any(), any(), any());
      verify(refreshTokenRepositoryImpl, never()).save(any());
    }

    @Test
    @Order(9)
    @DisplayName("異常系：無効（削除済み）のアカウントの場合、DisabledExceptionをスローすること")
    void loginWithPasskey_disabled() {
      stubAssertionLookup(USER_HANDLE);
      doReturn(account(0, false, true))
          .when(accountRepositoryImpl)
          .getByAccountNo(new AccountNo(1L));
      doReturn(3).when(loginConfig).getFailCount();

      assertThrows(DisabledException.class, () -> authServiceImpl.loginWithPasskey(JSON, IP));
      verify(webAuthnVerifier, never()).verifyAuthentication(any(), any(), any());
      verify(refreshTokenRepositoryImpl, never()).save(any());
    }

    @Test
    @Order(10)
    @DisplayName("異常系：署名の検証に失敗した場合、PasskeyVerificationExceptionをスローし、署名カウンタを更新しないこと")
    void loginWithPasskey_verificationFailed() throws Exception {
      stubAssertionLookup(USER_HANDLE);
      doReturn(account(0, false, false))
          .when(accountRepositoryImpl)
          .getByAccountNo(new AccountNo(1L));
      doReturn(3).when(loginConfig).getFailCount();
      doThrow(new PasskeyVerificationException("invalid signature"))
          .when(webAuthnVerifier)
          .verifyAuthentication(JSON, CHALLENGE, credential);

      assertThrows(
          PasskeyVerificationException.class, () -> authServiceImpl.loginWithPasskey(JSON, IP));
      verify(passkeyCredentialRepositoryImpl, never()).updateAuthenticationResult(any());
      verify(applicationEventPublisher, never()).publishEvent(any());
      verify(refreshTokenRepositoryImpl, never()).save(any());
      // パスキーの検証失敗はログイン失敗回数に加算しない
      verify(accountRepositoryImpl, never()).incrementLoginFailureCount(any());
    }

    @Test
    @Order(11)
    @DisplayName("異常系：パスキーの所有者アカウントが存在しない場合、PasskeyVerificationExceptionをスローすること")
    void loginWithPasskey_accountNotFound() {
      stubAssertionLookup(USER_HANDLE);
      doReturn(null).when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));

      assertThrows(
          PasskeyVerificationException.class, () -> authServiceImpl.loginWithPasskey(JSON, IP));
      verify(refreshTokenRepositoryImpl, never()).save(any());
    }
  }

  @Nested
  @Order(7)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class purgeExpiredWebAuthnChallenges {
    @Test
    @Order(1)
    @DisplayName("正常系：有効期限切れのWebAuthnチャレンジの削除をRepositoryに委譲すること")
    void purgeExpiredWebAuthnChallenges_success() {
      authServiceImpl.purgeExpiredWebAuthnChallenges();

      verify(webAuthnChallengeRepositoryImpl).deleteExpired();
    }
  }

  @Nested
  @Order(8)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class createRecoveryOptions {
    private static final AccountId ACCOUNT_ID = new AccountId("testuser1");
    private static final RecoveryCode CODE = new RecoveryCode("ABCD-EFGH-JKMN-PQRS");

    private AccountModel account(int loginFailureCount, boolean adminLocked, boolean deleted) {
      return AccountModel.builder()
          .accountNo(new AccountNo(1L))
          .accountId(ACCOUNT_ID)
          .accountName(new AccountName("テストユーザー"))
          .authorityKbn(AuthorityEnum.NORMAL)
          .loginFailureCount(new LoginFailureCount(loginFailureCount))
          .isAdminLocked(new IsAdminLocked(adminLocked))
          .isDeleted(new IsDeleted(deleted))
          .updatedAt(new UpdatedAt(OffsetDateTime.now(clock).minusMinutes(5)))
          .build();
    }

    @Test
    @Order(1)
    @DisplayName("正常系：リカバリーコードが一致した場合、コードを消費し、アカウントに紐づく復旧用チャレンジで登録オプションを返すこと")
    void createRecoveryOptions_success() {
      doReturn(account(0, false, false)).when(accountRepositoryImpl).getByAccountId(ACCOUNT_ID);
      doReturn(3).when(loginConfig).getFailCount();
      doReturn(true).when(recoveryCodeRepositoryImpl).consume(new AccountNo(1L), CODE);
      PasskeyChallenge challenge = new PasskeyChallenge("challenge-1");
      doReturn(challenge).when(webAuthnVerifier).generateChallenge();
      doReturn(300).when(webAuthnConfig).getChallengeTimeoutSeconds();
      doReturn("localhost").when(webAuthnConfig).getRpId();
      doReturn("Web Gallery").when(webAuthnConfig).getRpName();
      doReturn(new PasskeyUserHandle("handle-1"))
          .when(passkeyUserHandleRepositoryImpl)
          .findByAccountNo(new AccountNo(1L));
      doReturn(PasskeyCredentialModelList.empty())
          .when(passkeyCredentialRepositoryImpl)
          .findByAccountNo(new AccountNo(1L));

      PasskeyRegistrationOptionsModel actual =
          authServiceImpl.createRecoveryOptions(ACCOUNT_ID, CODE);

      assertEquals(challenge, actual.getChallenge());
      assertEquals(new PasskeyUserHandle("handle-1"), actual.getUserHandle());
      assertEquals(ACCOUNT_ID, actual.getAccountId());
      verify(accountRepositoryImpl).lockForLoginAttempt(ACCOUNT_ID);

      ArgumentCaptor<WebAuthnChallengeModel> captor =
          ArgumentCaptor.forClass(WebAuthnChallengeModel.class);
      verify(webAuthnChallengeRepositoryImpl).save(captor.capture());
      assertEquals(WebAuthnChallengePurposeEnum.RECOVERY, captor.getValue().getPurposeKbn());
      assertEquals(new AccountNo(1L), captor.getValue().getAccountNo());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：アカウントが存在しない場合、InvalidRecoveryCodeExceptionをスローし、チャレンジを発行しないこと")
    void createRecoveryOptions_accountNotFound() {
      doReturn(null).when(accountRepositoryImpl).getByAccountId(ACCOUNT_ID);

      assertThrows(
          InvalidRecoveryCodeException.class,
          () -> authServiceImpl.createRecoveryOptions(ACCOUNT_ID, CODE));
      verify(recoveryCodeRepositoryImpl, never()).consume(any(), any());
      verify(webAuthnChallengeRepositoryImpl, never()).save(any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：リカバリーコードが一致しない（使用済みを含む）場合、InvalidRecoveryCodeExceptionをスローし、ログイン失敗回数に加算しないこと")
    void createRecoveryOptions_codeMismatch() throws Exception {
      doReturn(account(0, false, false)).when(accountRepositoryImpl).getByAccountId(ACCOUNT_ID);
      doReturn(3).when(loginConfig).getFailCount();
      doReturn(false).when(recoveryCodeRepositoryImpl).consume(new AccountNo(1L), CODE);

      assertThrows(
          InvalidRecoveryCodeException.class,
          () -> authServiceImpl.createRecoveryOptions(ACCOUNT_ID, CODE));
      verify(webAuthnChallengeRepositoryImpl, never()).save(any());
      verify(accountRepositoryImpl, never()).incrementLoginFailureCount(any());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：管理者ロック中の場合、LockedExceptionをスローし、リカバリーコードを消費しないこと")
    void createRecoveryOptions_adminLocked() {
      doReturn(account(0, true, false)).when(accountRepositoryImpl).getByAccountId(ACCOUNT_ID);
      lenient().doReturn(3).when(loginConfig).getFailCount();

      assertThrows(
          LockedException.class, () -> authServiceImpl.createRecoveryOptions(ACCOUNT_ID, CODE));
      verify(recoveryCodeRepositoryImpl, never()).consume(any(), any());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：無効（削除済み）のアカウントの場合、InvalidRecoveryCodeExceptionをスローすること")
    void createRecoveryOptions_disabled() {
      doReturn(account(0, false, true)).when(accountRepositoryImpl).getByAccountId(ACCOUNT_ID);
      doReturn(3).when(loginConfig).getFailCount();

      assertThrows(
          InvalidRecoveryCodeException.class,
          () -> authServiceImpl.createRecoveryOptions(ACCOUNT_ID, CODE));
      verify(recoveryCodeRepositoryImpl, never()).consume(any(), any());
    }
  }

  @Nested
  @Order(9)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class loginWithRecoveryCode {
    private static final PasskeyResponseJson JSON = new PasskeyResponseJson("{\"id\":\"x\"}");
    private static final IpAddress IP = new IpAddress("127.0.0.1");
    private static final PasskeyChallenge CHALLENGE = new PasskeyChallenge("challenge-1");
    private static final PasskeyName NAME = new PasskeyName("新しいiPhone");

    private PasskeyAttestationModel attestation;

    @BeforeEach
    void setUp() {
      attestation =
          PasskeyAttestationModel.builder()
              .credentialId(new CredentialId("credential-new"))
              .attestedCredentialData(new AttestedCredentialData(new byte[] {1}))
              .signCount(new SignCount(0L))
              .transports(new PasskeyTransports("internal"))
              .isBackupEligible(new IsBackupEligible(true))
              .isBackedUp(new IsBackedUp(false))
              .isUvInitialized(new IsUvInitialized(true))
              .build();
    }

    private AccountModel account(int loginFailureCount, boolean adminLocked) {
      return AccountModel.builder()
          .accountNo(new AccountNo(1L))
          .accountId(new AccountId("testuser1"))
          .accountName(new AccountName("テストユーザー"))
          .authorityKbn(AuthorityEnum.NORMAL)
          .loginFailureCount(new LoginFailureCount(loginFailureCount))
          .isAdminLocked(new IsAdminLocked(adminLocked))
          .isDeleted(new IsDeleted(false))
          .updatedAt(new UpdatedAt(OffsetDateTime.now(clock).minusMinutes(5)))
          .build();
    }

    private void stubVerification(WebAuthnChallengeModel storedChallenge) {
      doReturn(CHALLENGE).when(webAuthnVerifier).readRegistrationChallenge(JSON);
      doReturn(attestation).when(webAuthnVerifier).verifyRegistration(JSON, CHALLENGE);
      doReturn(storedChallenge).when(webAuthnChallengeRepositoryImpl).consumeForRecovery(CHALLENGE);
    }

    private WebAuthnChallengeModel recoveryChallenge() {
      return WebAuthnChallengeModel.of(
          CHALLENGE,
          WebAuthnChallengePurposeEnum.RECOVERY,
          new AccountNo(1L),
          new ExpiresAt(OffsetDateTime.now(clock).plusMinutes(5)));
    }

    @Test
    @Order(1)
    @DisplayName("正常系：新しいパスキーを登録し、リカバリーコードによる復旧として認証成功イベントを発行してトークンを返すこと")
    void loginWithRecoveryCode_success() throws Exception {
      stubVerification(recoveryChallenge());
      doReturn(account(0, false)).when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
      doReturn(3).when(loginConfig).getFailCount();
      doReturn(null)
          .when(passkeyCredentialRepositoryImpl)
          .findByCredentialId(new CredentialId("credential-new"));
      doReturn("access-token").when(jwtTokenProvider).generateAccessToken(any());
      doReturn("refresh-token").when(jwtTokenProvider).generateRefreshToken();
      doReturn(7).when(jwtConfig).getRefreshTokenExpirationDays();
      doReturn(15).when(jwtConfig).getAccessTokenExpirationMinutes();

      AuthTokenModel actual = authServiceImpl.loginWithRecoveryCode(JSON, NAME, IP);

      assertEquals("access-token", actual.getAccessToken().value());
      verify(accountRepositoryImpl).lockForLoginAttempt(new AccountId("testuser1"));

      ArgumentCaptor<PasskeyCredentialModel> credentialCaptor =
          ArgumentCaptor.forClass(PasskeyCredentialModel.class);
      verify(passkeyCredentialRepositoryImpl).save(credentialCaptor.capture());
      assertEquals(new AccountNo(1L), credentialCaptor.getValue().getAccountNo());
      assertEquals(NAME, credentialCaptor.getValue().getPasskeyName());

      ArgumentCaptor<AuthenticationSuccessEvent> eventCaptor =
          ArgumentCaptor.forClass(AuthenticationSuccessEvent.class);
      verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
      assertEquals(
          LoginContextModel.of(IP, LoginMethodEnum.RECOVERY_CODE),
          eventCaptor.getValue().getAuthentication().getDetails());
      verify(refreshTokenRepositoryImpl).revokeAllByAccountNo(new AccountNo(1L));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：復旧用チャレンジが未発行・使用済み・期限切れの場合、PasskeyVerificationExceptionをスローしパスキーを登録しないこと")
    void loginWithRecoveryCode_challengeNotAvailable() throws Exception {
      stubVerification(null);

      assertThrows(
          PasskeyVerificationException.class,
          () -> authServiceImpl.loginWithRecoveryCode(JSON, NAME, IP));
      verify(passkeyCredentialRepositoryImpl, never()).save(any());
      verify(refreshTokenRepositoryImpl, never()).save(any());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：登録レスポンスの検証に失敗した場合、PasskeyVerificationExceptionをスローしチャレンジを消費しないこと")
    void loginWithRecoveryCode_verificationFailed() throws Exception {
      doReturn(CHALLENGE).when(webAuthnVerifier).readRegistrationChallenge(JSON);
      doThrow(new PasskeyVerificationException("origin mismatch"))
          .when(webAuthnVerifier)
          .verifyRegistration(JSON, CHALLENGE);

      assertThrows(
          PasskeyVerificationException.class,
          () -> authServiceImpl.loginWithRecoveryCode(JSON, NAME, IP));
      verify(webAuthnChallengeRepositoryImpl, never()).consumeForRecovery(any());
      verify(passkeyCredentialRepositoryImpl, never()).save(any());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：管理者ロック中の場合、LockedExceptionをスローしパスキーを登録しないこと")
    void loginWithRecoveryCode_adminLocked() throws Exception {
      stubVerification(recoveryChallenge());
      doReturn(account(0, true)).when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
      lenient().doReturn(3).when(loginConfig).getFailCount();

      assertThrows(
          LockedException.class, () -> authServiceImpl.loginWithRecoveryCode(JSON, NAME, IP));
      verify(passkeyCredentialRepositoryImpl, never()).save(any());
      verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：登録済みのパスキーの場合、GalleryException（パスキー重複）をスローすること")
    void loginWithRecoveryCode_duplicatePasskey() throws Exception {
      stubVerification(recoveryChallenge());
      doReturn(account(0, false)).when(accountRepositoryImpl).getByAccountNo(new AccountNo(1L));
      doReturn(3).when(loginConfig).getFailCount();
      doReturn(PasskeyCredentialModel.forRegist(new AccountNo(1L), attestation, NAME))
          .when(passkeyCredentialRepositoryImpl)
          .findByCredentialId(new CredentialId("credential-new"));

      GalleryException exception =
          assertThrows(
              GalleryException.class, () -> authServiceImpl.loginWithRecoveryCode(JSON, NAME, IP));
      assertEquals(ErrorEnum.DUPLICATE_PASSKEY.getErrorCode(), exception.getErrorCode());
      verify(passkeyCredentialRepositoryImpl, never()).save(any());
      verify(refreshTokenRepositoryImpl, never()).save(any());
    }
  }
}
