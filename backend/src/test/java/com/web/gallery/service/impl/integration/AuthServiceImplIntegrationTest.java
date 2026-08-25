package com.web.gallery.service.impl.integration;

import static org.junit.jupiter.api.Assertions.*;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import com.web.gallery.config.JwtConfig;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.account.Password;
import com.web.gallery.domain.auth.RefreshTokenValue;
import com.web.gallery.domain.common.TokenHash;
import com.web.gallery.model.AuthTokenModel;
import com.web.gallery.model.RefreshTokenModel;
import com.web.gallery.repository.RefreshTokenRepository;
import com.web.gallery.service.impl.AccountServiceImpl;
import com.web.gallery.service.impl.AuthServiceImpl;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class AuthServiceImplIntegrationTest {
	@Autowired
	private AuthServiceImpl authServiceImpl;

	@Autowired
	private AccountServiceImpl accountServiceImpl;

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtConfig jwtConfig;

	private static final String TEST_PASSWORD = "password123";

	/**
	 * テストデータ投入
	 */
	private void insertTestData() {
		String hashedPassword = passwordEncoder.encode(TEST_PASSWORD);

		// 正常なアカウント（ログイン失敗回数0）
		jdbcTemplate.update(
			"INSERT INTO common.account VALUES(1, 1, '2000-01-01 09:00:00 Asia/Tokyo', 1, '2001-01-01 09:00:00 Asia/Tokyo', false, 'testuser01', 'テストユーザー01', ?, '1991-02-14', 'none', 'none', 'none', '', 'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 0)",
			hashedPassword
		);

		// ロック状態のアカウント（ログイン失敗回数3）
		jdbcTemplate.update(
			"INSERT INTO common.account VALUES(2, 2, '2000-01-02 09:00:00 Asia/Tokyo', 2, '2001-01-02 09:00:00 Asia/Tokyo', false, 'lockeduser', 'ロックユーザー', ?, '1991-02-14', 'none', 'none', 'none', '', 'administrator', '2002-01-01 09:00:00 Asia/Tokyo', 3)",
			hashedPassword
		);

		jdbcTemplate.update("ALTER SEQUENCE common.account_account_no_seq RESTART 3");
	}

	/**
	 * トークンをSHA-256でハッシュ化する（プロダクションコードと同じロジック）
	 */
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
	class login {
		@BeforeEach
		void setUp() {
			insertTestData();
		}

		@Test
		@Order(1)
		@DisplayName("正常系：ログイン成功")
		void login_success() throws Exception {
			OffsetDateTime beforeLogin = OffsetDateTime.now();
			AuthTokenModel result = authServiceImpl.login(new AccountId("testuser01"), new Password(TEST_PASSWORD));
			OffsetDateTime afterLogin = OffsetDateTime.now();

			assertNotNull(result.getAccessToken().value());
			assertFalse(result.getAccessToken().value().isEmpty());
			assertNotNull(result.getRefreshToken().value());
			assertFalse(result.getRefreshToken().value().isEmpty());
			assertTrue(result.getExpiresIn().value() > 0);

			// リフレッシュトークンがDBに保存されていることを検証
			String tokenHash = hashToken(result.getRefreshToken().value());
			RefreshTokenModel storedToken = refreshTokenRepository.findByTokenHash(new TokenHash(tokenHash));
			assertNotNull(storedToken);
			assertEquals(1L, storedToken.getAccountNo().value());
			assertFalse(storedToken.getIsRevoked().value());
			assertFalse(storedToken.getExpiresAt().value().isBefore(beforeLogin.plusDays(jwtConfig.getRefreshTokenExpirationDays())));
			assertFalse(storedToken.getExpiresAt().value().isAfter(afterLogin.plusDays(jwtConfig.getRefreshTokenExpirationDays())));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：再ログイン時に既存のリフレッシュトークンが無効化される")
		void login_revokes_existing_tokens() throws Exception {
			// 1回目のログイン
			AuthTokenModel firstResult = authServiceImpl.login(new AccountId("testuser01"), new Password(TEST_PASSWORD));
			String firstTokenHash = hashToken(firstResult.getRefreshToken().value());

			// 2回目のログイン
			AuthTokenModel secondResult = authServiceImpl.login(new AccountId("testuser01"), new Password(TEST_PASSWORD));
			String secondTokenHash = hashToken(secondResult.getRefreshToken().value());

			// 1回目のトークンが無効化されていることを検証
			RefreshTokenModel firstToken = refreshTokenRepository.findByTokenHash(new TokenHash(firstTokenHash));
			assertNotNull(firstToken);
			assertTrue(firstToken.getIsRevoked().value());

			// 2回目のトークンが有効であることを検証
			RefreshTokenModel secondToken = refreshTokenRepository.findByTokenHash(new TokenHash(secondTokenHash));
			assertNotNull(secondToken);
			assertFalse(secondToken.getIsRevoked().value());
		}

		@Test
		@Order(3)
		@DisplayName("異常系：パスワード不一致の場合、BadCredentialsExceptionをthrowする")
		void login_wrong_password() {
			assertThrows(BadCredentialsException.class,
				() -> authServiceImpl.login(new AccountId("testuser01"), new Password("wrongpassword")));
		}

		@Test
		@Order(4)
		@DisplayName("異常系：存在しないアカウントIDの場合、BadCredentialsExceptionをthrowする")
		void login_account_not_found() {
			assertThrows(BadCredentialsException.class,
				() -> authServiceImpl.login(new AccountId("notexists"), new Password(TEST_PASSWORD)));
		}

		@Test
		@Order(5)
		@DisplayName("異常系：アカウントロックの場合、LockedException をthrowする")
		void login_locked_account() {
			assertThrows(LockedException.class,
				() -> authServiceImpl.login(new AccountId("lockeduser"), new Password(TEST_PASSWORD)));
		}
	}

	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	class refresh {
		@BeforeEach
		void setUp() {
			insertTestData();
		}

		@Test
		@Order(1)
		@DisplayName("正常系：リフレッシュ成功")
		void refresh_success() {
			// ログインしてリフレッシュトークンを取得
			AuthTokenModel loginResult = authServiceImpl.login(new AccountId("testuser01"), new Password(TEST_PASSWORD));

			// リフレッシュ
			AuthTokenModel refreshResult = authServiceImpl.refresh(loginResult.getRefreshToken());

			assertNotNull(refreshResult.getAccessToken().value());
			assertFalse(refreshResult.getAccessToken().value().isEmpty());
			assertEquals(loginResult.getRefreshToken().value(), refreshResult.getRefreshToken().value());
			assertTrue(refreshResult.getExpiresIn().value() > 0);
		}

		@Test
		@Order(2)
		@DisplayName("異常系：存在しないリフレッシュトークンの場合、IllegalArgumentExceptionをthrowする")
		void refresh_invalid_token() {
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> authServiceImpl.refresh(new RefreshTokenValue("invalid-refresh-token")));
			assertEquals("無効なリフレッシュトークンです", exception.getMessage());
		}

		@Test
		@Order(3)
		@DisplayName("異常系：無効化済みリフレッシュトークンの場合、IllegalArgumentExceptionをthrowする")
		void refresh_revoked_token() throws Exception {
			// ログインしてリフレッシュトークンを取得
			AuthTokenModel loginResult = authServiceImpl.login(new AccountId("testuser01"), new Password(TEST_PASSWORD));
			String refreshToken = loginResult.getRefreshToken().value();

			// トークンを無効化
			refreshTokenRepository.revokeByTokenHash(new TokenHash(hashToken(refreshToken)));

			// 無効化済みトークンでリフレッシュ
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> authServiceImpl.refresh(new RefreshTokenValue(refreshToken)));
			assertEquals("無効なリフレッシュトークンです", exception.getMessage());
		}

		@Test
		@Order(4)
		@DisplayName("異常系：有効期限切れリフレッシュトークンの場合、IllegalArgumentExceptionをthrowする")
		void refresh_expired_token() throws Exception {
			// ログインしてリフレッシュトークンを取得
			AuthTokenModel loginResult = authServiceImpl.login(new AccountId("testuser01"), new Password(TEST_PASSWORD));
			String refreshToken = loginResult.getRefreshToken().value();

			// 有効期限を過去に設定
			String tokenHash = hashToken(refreshToken);
			jdbcTemplate.update(
				"UPDATE common.refresh_token SET expires_at = ? WHERE token_hash = ?",
				OffsetDateTime.now().minusDays(1), tokenHash
			);

			// 有効期限切れトークンでリフレッシュ
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> authServiceImpl.refresh(new RefreshTokenValue(refreshToken)));
			assertEquals("リフレッシュトークンの有効期限が切れています", exception.getMessage());
		}

		@Test
		@Order(5)
		@DisplayName("異常系：発行後にアカウントがロックされた場合、LockedExceptionをthrowする")
		void refresh_account_locked_after_token_issued() {
			// ログインしてリフレッシュトークンを取得
			AuthTokenModel loginResult = authServiceImpl.login(new AccountId("testuser01"), new Password(TEST_PASSWORD));
			String refreshToken = loginResult.getRefreshToken().value();

			// 管理者によるアカウントロックを模擬（ログイン失敗回数を上限に更新）
			jdbcTemplate.update(
				"UPDATE common.account SET login_failure_count = 3 WHERE account_no = 1"
			);

			// ロック後のリフレッシュはLockedExceptionをthrowする
			assertThrows(LockedException.class,
				() -> authServiceImpl.refresh(new RefreshTokenValue(refreshToken)));
		}

		@Test
		@Order(6)
		@DisplayName("異常系：アカウント削除後のリフレッシュトークンの場合、NPEではなくIllegalArgumentExceptionをthrowする")
		void refresh_after_account_deleted() {
			// ログインしてリフレッシュトークンを取得
			AuthTokenModel loginResult = authServiceImpl.login(new AccountId("testuser01"), new Password(TEST_PASSWORD));
			String refreshToken = loginResult.getRefreshToken().value();

			// アカウントを削除（本来はdeleteAccount内でリフレッシュトークンも失効するが、
			// 失効漏れがあった場合の防御的なnullチェックを検証するため直接アカウントのみ削除する）
			jdbcTemplate.update("DELETE FROM common.account WHERE account_no = ?", 1L);

			// 削除済みアカウントのリフレッシュトークンでリフレッシュ
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> authServiceImpl.refresh(new RefreshTokenValue(refreshToken)));
			assertEquals("無効なリフレッシュトークンです", exception.getMessage());
		}

		@Test
		@Order(7)
		@DisplayName("異常系：アカウント削除によりリフレッシュトークンが失効し、リフレッシュに失敗する")
		void refresh_fails_after_delete_account() {
			// ログインしてリフレッシュトークンを取得
			AuthTokenModel loginResult = authServiceImpl.login(new AccountId("testuser01"), new Password(TEST_PASSWORD));
			String refreshToken = loginResult.getRefreshToken().value();

			// アカウントを削除（deleteAccount内でリフレッシュトークンも失効される）
			accountServiceImpl.deleteAccount(new AccountNo(1L), new AccountId("testuser01"));

			// 削除済みアカウントのリフレッシュトークンでリフレッシュ
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> authServiceImpl.refresh(new RefreshTokenValue(refreshToken)));
			assertEquals("無効なリフレッシュトークンです", exception.getMessage());
		}
	}

	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@Sql("/sql/common/cleanup.sql")
	class logout {
		@BeforeEach
		void setUp() {
			insertTestData();
		}

		@Test
		@Order(1)
		@DisplayName("正常系：ログアウト成功（リフレッシュトークンが無効化される）")
		void logout_success() throws Exception {
			// ログインしてリフレッシュトークンを取得
			AuthTokenModel loginResult = authServiceImpl.login(new AccountId("testuser01"), new Password(TEST_PASSWORD));
			String refreshToken = loginResult.getRefreshToken().value();
			String tokenHash = hashToken(refreshToken);

			// ログアウト前はトークンが有効
			RefreshTokenModel beforeLogout = refreshTokenRepository.findByTokenHash(new TokenHash(tokenHash));
			assertNotNull(beforeLogout);
			assertFalse(beforeLogout.getIsRevoked().value());

			// ログアウト
			authServiceImpl.logout(new RefreshTokenValue(refreshToken));

			// ログアウト後はトークンが無効化されている
			RefreshTokenModel afterLogout = refreshTokenRepository.findByTokenHash(new TokenHash(tokenHash));
			assertNotNull(afterLogout);
			assertTrue(afterLogout.getIsRevoked().value());
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
			// ログイン
			AuthTokenModel loginResult = authServiceImpl.login(new AccountId("testuser01"), new Password(TEST_PASSWORD));
			String refreshToken = loginResult.getRefreshToken().value();

			// ログアウト
			authServiceImpl.logout(new RefreshTokenValue(refreshToken));

			// ログアウト後のリフレッシュは失敗
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> authServiceImpl.refresh(new RefreshTokenValue(refreshToken)));
			assertEquals("無効なリフレッシュトークンです", exception.getMessage());
		}
	}
}
