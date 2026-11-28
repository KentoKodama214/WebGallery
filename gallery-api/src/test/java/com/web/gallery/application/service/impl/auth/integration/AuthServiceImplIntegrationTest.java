package com.web.gallery.application.service.impl.auth.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.application.config.JwtConfig;
import com.web.gallery.application.model.account.AccountModel;
import com.web.gallery.application.model.auth.AuthTokenModel;
import com.web.gallery.application.model.auth.PasskeyAuthenticationOptionsModel;
import com.web.gallery.application.model.auth.PasskeyRegistrationOptionsModel;
import com.web.gallery.application.model.auth.RecoveryCodeList;
import com.web.gallery.application.model.auth.RefreshTokenModel;
import com.web.gallery.application.repository.auth.RefreshTokenRepository;
import com.web.gallery.application.service.account.AccountService;
import com.web.gallery.application.service.impl.auth.AuthServiceImpl;
import com.web.gallery.domain.constant.MessageConst;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.exception.InvalidRecoveryCodeException;
import com.web.gallery.domain.exception.InvalidRefreshTokenException;
import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountName;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.account.LoginFailureCount;
import com.web.gallery.domain.model.auth.PasskeyName;
import com.web.gallery.domain.model.auth.PasskeyResponseJson;
import com.web.gallery.domain.model.auth.RecoveryCode;
import com.web.gallery.domain.model.auth.RefreshTokenValue;
import com.web.gallery.domain.model.common.IpAddress;
import com.web.gallery.domain.model.common.TokenHash;
import com.web.gallery.testsupport.PasskeyTestAuthenticator;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.LockedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
public class AuthServiceImplIntegrationTest {
  @Autowired private AuthServiceImpl authServiceImpl;

  @Autowired private AccountService accountServiceImpl;

  @Autowired private RefreshTokenRepository refreshTokenRepository;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Autowired private JwtConfig jwtConfig;

  private static final String TEST_ACCOUNT_ID = "testuser01";

  private static final IpAddress TEST_IP_ADDRESS = new IpAddress("203.0.113.1");

  /** テストアカウントのパスキーを保持する擬似認証器 */
  private PasskeyTestAuthenticator authenticator;

  /** アカウント登録時に発行されたリカバリーコード */
  private RecoveryCodeList recoveryCodes;

  /**
   * テストデータ投入
   *
   * <p>アカウント番号1のテストアカウントを、本番と同じ新規登録フロー（パスキーの作成）で登録する
   */
  private void insertTestData() throws GalleryException {
    jdbcTemplate.update("ALTER SEQUENCE common.account_account_no_seq RESTART 1");
    authenticator = new PasskeyTestAuthenticator();

    PasskeyRegistrationOptionsModel options =
        accountServiceImpl.createSignupOptions(
            new AccountId(TEST_ACCOUNT_ID), new AccountName("テストユーザー01"));
    String registrationJson =
        authenticator.createRegistrationResponseJson(
            PasskeyTestAuthenticator.RP_ID,
            options.getChallenge().value(),
            options.getUserHandle().value(),
            TEST_ACCOUNT_ID);
    recoveryCodes =
        accountServiceImpl.registAccount(
            AccountModel.builder()
                .accountId(new AccountId(TEST_ACCOUNT_ID))
                .accountName(new AccountName("テストユーザー01"))
                .loginFailureCount(new LoginFailureCount(0))
                .build(),
            new PasskeyResponseJson(registrationJson),
            new PasskeyName("テスト用パスキー"));
  }

  /**
   * テストアカウントのパスキーでログインする
   *
   * @return {@link AuthTokenModel}
   */
  private AuthTokenModel loginWithPasskey() {
    PasskeyAuthenticationOptionsModel options =
        authServiceImpl.createPasskeyAuthenticationOptions();
    return authServiceImpl.loginWithPasskey(
        new PasskeyResponseJson(
            authenticator.createAuthenticationResponseJson(
                PasskeyTestAuthenticator.RP_ID, options.getChallenge().value())),
        TEST_IP_ADDRESS);
  }

  /**
   * テストアカウントのパスキーで本人確認（再認証）用の認証レスポンスを作る
   *
   * @return 本人確認用の認証レスポンス
   */
  private PasskeyResponseJson reauthenticate() {
    PasskeyAuthenticationOptionsModel options =
        accountServiceImpl.createReauthenticationOptions(new AccountNo(1L));
    return new PasskeyResponseJson(
        authenticator.createAuthenticationResponseJson(
            PasskeyTestAuthenticator.RP_ID, options.getChallenge().value()));
  }

  /** トークンをSHA-256でハッシュ化する（プロダクションコードと同じロジック） */
  private String hashToken(String token) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
    StringBuilder hexString = new StringBuilder();
    for (byte b : hash) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) hexString.append('0');
      hexString.append(hex);
    }
    return hexString.toString();
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  class loginWithPasskey {
    @BeforeEach
    void setUp() throws GalleryException {
      insertTestData();
    }

    @Test
    @Order(1)
    @DisplayName("正常系：パスキーでログインでき、リフレッシュトークンが保存され、ログイン履歴にパスキーとして記録される")
    void loginWithPasskey_success() throws Exception {
      OffsetDateTime beforeLogin = OffsetDateTime.now();
      AuthTokenModel result = loginWithPasskey();
      OffsetDateTime afterLogin = OffsetDateTime.now();

      assertFalse(result.getAccessToken().value().isEmpty());
      assertFalse(result.getRefreshToken().value().isEmpty());
      assertTrue(result.getExpiresIn().value() > 0);

      RefreshTokenModel storedToken =
          refreshTokenRepository.findByTokenHash(
              new TokenHash(hashToken(result.getRefreshToken().value())));
      assertNotNull(storedToken);
      assertEquals(1L, storedToken.getAccountNo().value());
      assertFalse(storedToken.getIsRevoked().value());
      assertFalse(
          storedToken
              .getExpiresAt()
              .value()
              .isBefore(beforeLogin.plusDays(jwtConfig.getRefreshTokenExpirationDays())));
      assertFalse(
          storedToken
              .getExpiresAt()
              .value()
              .isAfter(afterLogin.plusDays(jwtConfig.getRefreshTokenExpirationDays())));

      String loginMethod =
          jdbcTemplate.queryForObject(
              "SELECT login_method_kbn::text FROM common.login_history WHERE account_no = 1",
              String.class);
      assertEquals("passkey", loginMethod);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：再ログイン時に既存のリフレッシュトークンが無効化される")
    void loginWithPasskey_revokes_existing_tokens() throws Exception {
      String firstTokenHash = hashToken(loginWithPasskey().getRefreshToken().value());
      String secondTokenHash = hashToken(loginWithPasskey().getRefreshToken().value());

      assertTrue(
          refreshTokenRepository
              .findByTokenHash(new TokenHash(firstTokenHash))
              .getIsRevoked()
              .value());
      assertFalse(
          refreshTokenRepository
              .findByTokenHash(new TokenHash(secondTokenHash))
              .getIsRevoked()
              .value());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：管理者ロック中の場合、LockedExceptionをthrowする")
    void loginWithPasskey_admin_locked() {
      jdbcTemplate.update(
          "UPDATE common.account SET is_admin_locked = true, login_failure_count = 3, updated_at = NOW() WHERE account_no = 1");

      assertThrows(LockedException.class, () -> loginWithPasskey());
    }

    @Test
    @Order(4)
    @DisplayName("正常系：ログイン失敗回数によるロックは解除時間経過後に自動解除され、パスキーでログインできる")
    void loginWithPasskey_autoUnlocks_afterLockDurationElapsed() {
      // lockDurationMinutes（テスト設定で30分）より前に更新されたことにする
      jdbcTemplate.update(
          "UPDATE common.account SET login_failure_count = 3, updated_at = ? WHERE account_no = 1",
          OffsetDateTime.now().minusMinutes(31));

      assertNotNull(loginWithPasskey().getAccessToken().value());

      Integer failureCount =
          jdbcTemplate.queryForObject(
              "SELECT login_failure_count FROM common.account WHERE account_no = 1", Integer.class);
      assertEquals(0, failureCount);
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  class refresh {
    @BeforeEach
    void setUp() throws GalleryException {
      insertTestData();
    }

    @Test
    @Order(1)
    @DisplayName("正常系：リフレッシュ成功時、リフレッシュトークンがローテーション（新規発行）されること")
    void refresh_success() {
      AuthTokenModel loginResult = loginWithPasskey();

      AuthTokenModel refreshResult = authServiceImpl.refresh(loginResult.getRefreshToken());

      assertNotNull(refreshResult.getAccessToken().value());
      assertFalse(refreshResult.getAccessToken().value().isEmpty());
      assertNotEquals(
          loginResult.getRefreshToken().value(), refreshResult.getRefreshToken().value());
      assertTrue(refreshResult.getExpiresIn().value() > 0);
    }

    @Test
    @Order(2)
    @DisplayName("異常系：ローテーション済み（無効化済み）トークンを再利用した場合、盗用とみなし該当アカウントの新トークンも無効化されること")
    void refresh_reuseDetection_revokesAllTokens() {
      String firstRefreshToken = loginWithPasskey().getRefreshToken().value();

      // 1回目のリフレッシュでトークンがローテーションされる
      AuthTokenModel refreshResult =
          authServiceImpl.refresh(new RefreshTokenValue(firstRefreshToken));
      String rotatedRefreshToken = refreshResult.getRefreshToken().value();

      // 無効化済みの旧トークンを再利用（盗用シナリオ）
      assertThrows(
          InvalidRefreshTokenException.class,
          () -> authServiceImpl.refresh(new RefreshTokenValue(firstRefreshToken)));

      // 再利用検知により、ローテーション後の新トークンも失効していること
      InvalidRefreshTokenException exception =
          assertThrows(
              InvalidRefreshTokenException.class,
              () -> authServiceImpl.refresh(new RefreshTokenValue(rotatedRefreshToken)));
      assertEquals(MessageConst.ERR_INVALID_REFRESH_TOKEN, exception.getMessage());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：存在しないリフレッシュトークンの場合、InvalidRefreshTokenExceptionをthrowする")
    void refresh_invalid_token() {
      InvalidRefreshTokenException exception =
          assertThrows(
              InvalidRefreshTokenException.class,
              () -> authServiceImpl.refresh(new RefreshTokenValue("invalid-refresh-token")));
      assertEquals(MessageConst.ERR_INVALID_REFRESH_TOKEN, exception.getMessage());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：無効化済みリフレッシュトークンの場合、InvalidRefreshTokenExceptionをthrowする")
    void refresh_revoked_token() throws Exception {
      String refreshToken = loginWithPasskey().getRefreshToken().value();

      refreshTokenRepository.revokeByTokenHash(new TokenHash(hashToken(refreshToken)));

      InvalidRefreshTokenException exception =
          assertThrows(
              InvalidRefreshTokenException.class,
              () -> authServiceImpl.refresh(new RefreshTokenValue(refreshToken)));
      assertEquals(MessageConst.ERR_INVALID_REFRESH_TOKEN, exception.getMessage());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：有効期限切れリフレッシュトークンの場合、InvalidRefreshTokenExceptionをthrowする")
    void refresh_expired_token() throws Exception {
      String refreshToken = loginWithPasskey().getRefreshToken().value();

      jdbcTemplate.update(
          "UPDATE common.refresh_token SET expires_at = ? WHERE token_hash = ?",
          OffsetDateTime.now().minusDays(1),
          hashToken(refreshToken));

      InvalidRefreshTokenException exception =
          assertThrows(
              InvalidRefreshTokenException.class,
              () -> authServiceImpl.refresh(new RefreshTokenValue(refreshToken)));
      assertEquals(MessageConst.ERR_REFRESH_TOKEN_EXPIRED, exception.getMessage());
    }

    @Test
    @Order(6)
    @DisplayName("異常系：発行後にアカウントがロックされた場合、LockedExceptionをthrowする")
    void refresh_account_locked_after_token_issued() {
      String refreshToken = loginWithPasskey().getRefreshToken().value();

      // 管理者によるアカウントロックを模擬（ログイン失敗回数を上限に更新し、更新日時も現在時刻にする）
      jdbcTemplate.update(
          "UPDATE common.account SET login_failure_count = 3, updated_at = NOW() WHERE account_no = 1");

      assertThrows(
          LockedException.class,
          () -> authServiceImpl.refresh(new RefreshTokenValue(refreshToken)));
    }

    @Test
    @Order(7)
    @DisplayName("正常系：ロック解除時間経過後はリフレッシュ時に自動解除され、リフレッシュが成功する")
    void refresh_autoUnlocks_afterLockDurationElapsed() {
      String refreshToken = loginWithPasskey().getRefreshToken().value();

      jdbcTemplate.update(
          "UPDATE common.account SET login_failure_count = 3, updated_at = ? WHERE account_no = 1",
          OffsetDateTime.now().minusMinutes(31));

      AuthTokenModel refreshResult = authServiceImpl.refresh(new RefreshTokenValue(refreshToken));

      assertNotNull(refreshResult.getAccessToken().value());
      Integer failureCount =
          jdbcTemplate.queryForObject(
              "SELECT login_failure_count FROM common.account WHERE account_no = 1", Integer.class);
      assertEquals(0, failureCount);
    }

    @Test
    @Order(8)
    @DisplayName("異常系：アカウント削除後のリフレッシュトークンの場合、NPEではなくInvalidRefreshTokenExceptionをthrowする")
    void refresh_after_account_deleted() {
      String refreshToken = loginWithPasskey().getRefreshToken().value();

      // アカウントを削除（本来はdeleteAccount内でリフレッシュトークンも失効するが、
      // 失効漏れがあった場合の防御的なnullチェックを検証するため直接アカウントのみ削除する）。
      // 外部キー制約付きの関連行は、アプリの削除フロー同様に先に削除しておく
      jdbcTemplate.update("DELETE FROM common.login_history WHERE account_no = ?", 1L);
      jdbcTemplate.update("DELETE FROM common.passkey_credential WHERE account_no = ?", 1L);
      jdbcTemplate.update("DELETE FROM common.passkey_user_handle WHERE account_no = ?", 1L);
      jdbcTemplate.update("DELETE FROM common.recovery_code WHERE account_no = ?", 1L);
      jdbcTemplate.update("DELETE FROM common.account_authority WHERE account_no = ?", 1L);
      jdbcTemplate.update("DELETE FROM common.account WHERE account_no = ?", 1L);

      InvalidRefreshTokenException exception =
          assertThrows(
              InvalidRefreshTokenException.class,
              () -> authServiceImpl.refresh(new RefreshTokenValue(refreshToken)));
      assertEquals(MessageConst.ERR_INVALID_REFRESH_TOKEN, exception.getMessage());
    }

    @Test
    @Order(9)
    @DisplayName("異常系：アカウント削除によりリフレッシュトークンが失効し、リフレッシュに失敗する")
    void refresh_fails_after_delete_account() throws GalleryException {
      String refreshToken = loginWithPasskey().getRefreshToken().value();

      // アカウントを削除（deleteAccount内でリフレッシュトークンも失効される）
      accountServiceImpl.deleteAccount(
          new AccountNo(1L), new AccountId(TEST_ACCOUNT_ID), reauthenticate());

      InvalidRefreshTokenException exception =
          assertThrows(
              InvalidRefreshTokenException.class,
              () -> authServiceImpl.refresh(new RefreshTokenValue(refreshToken)));
      assertEquals(MessageConst.ERR_INVALID_REFRESH_TOKEN, exception.getMessage());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  class logout {
    @BeforeEach
    void setUp() throws GalleryException {
      insertTestData();
    }

    @Test
    @Order(1)
    @DisplayName("正常系：ログアウト成功（リフレッシュトークンが無効化される）")
    void logout_success() throws Exception {
      String refreshToken = loginWithPasskey().getRefreshToken().value();
      String tokenHash = hashToken(refreshToken);

      assertFalse(
          refreshTokenRepository.findByTokenHash(new TokenHash(tokenHash)).getIsRevoked().value());

      authServiceImpl.logout(new RefreshTokenValue(refreshToken));

      assertTrue(
          refreshTokenRepository.findByTokenHash(new TokenHash(tokenHash)).getIsRevoked().value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：存在しないリフレッシュトークンでログアウトしてもエラーにならない")
    void logout_with_nonexistent_token() {
      assertDoesNotThrow(() -> authServiceImpl.logout(new RefreshTokenValue("nonexistent-token")));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：ログアウト後にリフレッシュが失敗する")
    void logout_then_refresh_fails() {
      String refreshToken = loginWithPasskey().getRefreshToken().value();

      authServiceImpl.logout(new RefreshTokenValue(refreshToken));

      InvalidRefreshTokenException exception =
          assertThrows(
              InvalidRefreshTokenException.class,
              () -> authServiceImpl.refresh(new RefreshTokenValue(refreshToken)));
      assertEquals(MessageConst.ERR_INVALID_REFRESH_TOKEN, exception.getMessage());
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Sql("/sql/common/cleanup.sql")
  class loginWithRecoveryCode {
    @BeforeEach
    void setUp() throws GalleryException {
      insertTestData();
    }

    /**
     * リカバリーコードで新しい認証器にパスキーを作成し、復旧ログインする
     *
     * @param newAuthenticator 新しい認証器（端末を失った後の新しい端末に相当）
     * @param recoveryCode リカバリーコード
     * @return {@link AuthTokenModel}
     */
    private AuthTokenModel recover(
        PasskeyTestAuthenticator newAuthenticator, RecoveryCode recoveryCode)
        throws GalleryException {
      PasskeyRegistrationOptionsModel options =
          authServiceImpl.createRecoveryOptions(new AccountId(TEST_ACCOUNT_ID), recoveryCode);
      String registrationJson =
          newAuthenticator.createRegistrationResponseJson(
              PasskeyTestAuthenticator.RP_ID,
              options.getChallenge().value(),
              options.getUserHandle().value(),
              TEST_ACCOUNT_ID);
      return authServiceImpl.loginWithRecoveryCode(
          new PasskeyResponseJson(registrationJson), new PasskeyName("新しい端末"), TEST_IP_ADDRESS);
    }

    @Test
    @Order(1)
    @DisplayName("正常系：リカバリーコードで新しいパスキーを登録してログインでき、以後はそのパスキーでログインでき、コードは使用済みになる")
    void loginWithRecoveryCode_success() throws GalleryException {
      PasskeyTestAuthenticator newAuthenticator = new PasskeyTestAuthenticator();

      AuthTokenModel result = recover(newAuthenticator, recoveryCodes.get(0));

      assertFalse(result.getAccessToken().value().isEmpty());
      // 新しいパスキーが追加され、リカバリーコードが1件消費されている
      assertEquals(
          2,
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM common.passkey_credential WHERE account_no = 1",
              Integer.class));
      assertEquals(9, accountServiceImpl.getRecoveryCodeCount(new AccountNo(1L)));
      assertEquals(
          "recovery_code",
          jdbcTemplate.queryForObject(
              "SELECT login_method_kbn::text FROM common.login_history WHERE account_no = 1",
              String.class));

      // 新しい端末のパスキーでログインできる
      authenticator = newAuthenticator;
      assertNotNull(loginWithPasskey().getAccessToken().value());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：使用済みのリカバリーコードは再利用できない")
    void loginWithRecoveryCode_used_code_rejected() throws GalleryException {
      recover(new PasskeyTestAuthenticator(), recoveryCodes.get(0));

      assertThrows(
          InvalidRecoveryCodeException.class,
          () ->
              authServiceImpl.createRecoveryOptions(
                  new AccountId(TEST_ACCOUNT_ID), recoveryCodes.get(0)));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：ハイフンなし・小文字で入力したリカバリーコードでも照合できる")
    void loginWithRecoveryCode_normalized_input() throws GalleryException {
      RecoveryCode typed =
          new RecoveryCode(recoveryCodes.get(1).value().toLowerCase(java.util.Locale.ROOT));

      assertNotNull(recover(new PasskeyTestAuthenticator(), typed).getAccessToken().value());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：存在しないアカウント・誤ったリカバリーコードの場合、InvalidRecoveryCodeExceptionをthrowする")
    void loginWithRecoveryCode_invalid() {
      assertThrows(
          InvalidRecoveryCodeException.class,
          () ->
              authServiceImpl.createRecoveryOptions(
                  new AccountId("notexists"), recoveryCodes.get(0)));
      assertThrows(
          InvalidRecoveryCodeException.class,
          () ->
              authServiceImpl.createRecoveryOptions(
                  new AccountId(TEST_ACCOUNT_ID), new RecoveryCode("AAAA-AAAA-AAAA-AAAA")));
      assertEquals(10, accountServiceImpl.getRecoveryCodeCount(new AccountNo(1L)));
    }

    @Test
    @Order(5)
    @DisplayName("異常系：リカバリーコードの再発行後は、以前のコードは使えない")
    void loginWithRecoveryCode_old_codes_invalid_after_regeneration() throws GalleryException {
      RecoveryCodeList regenerated =
          accountServiceImpl.regenerateRecoveryCodes(new AccountNo(1L), reauthenticate());

      assertThrows(
          InvalidRecoveryCodeException.class,
          () ->
              authServiceImpl.createRecoveryOptions(
                  new AccountId(TEST_ACCOUNT_ID), recoveryCodes.get(0)));
      assertNotNull(recover(new PasskeyTestAuthenticator(), regenerated.get(0)));
    }
  }
}
