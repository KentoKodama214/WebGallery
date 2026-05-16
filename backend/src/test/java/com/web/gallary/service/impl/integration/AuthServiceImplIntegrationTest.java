package com.web.gallary.service.impl.integration;

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

import com.web.gallary.model.AuthTokenModel;
import com.web.gallary.model.RefreshTokenModel;
import com.web.gallary.repository.RefreshTokenRepository;
import com.web.gallary.service.impl.AuthServiceImpl;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class AuthServiceImplIntegrationTest {
	@Autowired
	private AuthServiceImpl authServiceImpl;

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PasswordEncoder passwordEncoder;

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
			AuthTokenModel result = authServiceImpl.login("testuser01", TEST_PASSWORD);

			assertNotNull(result.getAccessToken());
			assertFalse(result.getAccessToken().isEmpty());
			assertNotNull(result.getRefreshToken());
			assertFalse(result.getRefreshToken().isEmpty());
			assertTrue(result.getExpiresIn() > 0);

			// リフレッシュトークンがDBに保存されていることを検証
			String tokenHash = hashToken(result.getRefreshToken());
			RefreshTokenModel storedToken = refreshTokenRepository.findByTokenHash(tokenHash);
			assertNotNull(storedToken);
			assertEquals(1, storedToken.getAccountNo());
			assertFalse(storedToken.getIsRevoked());
			assertTrue(storedToken.getExpiresAt().isAfter(OffsetDateTime.now()));
		}

		@Test
		@Order(2)
		@DisplayName("正常系：再ログイン時に既存のリフレッシュトークンが無効化される")
		void login_revokes_existing_tokens() throws Exception {
			// 1回目のログイン
			AuthTokenModel firstResult = authServiceImpl.login("testuser01", TEST_PASSWORD);
			String firstTokenHash = hashToken(firstResult.getRefreshToken());

			// 2回目のログイン
			AuthTokenModel secondResult = authServiceImpl.login("testuser01", TEST_PASSWORD);
			String secondTokenHash = hashToken(secondResult.getRefreshToken());

			// 1回目のトークンが無効化されていることを検証
			RefreshTokenModel firstToken = refreshTokenRepository.findByTokenHash(firstTokenHash);
			assertNotNull(firstToken);
			assertTrue(firstToken.getIsRevoked());

			// 2回目のトークンが有効であることを検証
			RefreshTokenModel secondToken = refreshTokenRepository.findByTokenHash(secondTokenHash);
			assertNotNull(secondToken);
			assertFalse(secondToken.getIsRevoked());
		}

		@Test
		@Order(3)
		@DisplayName("異常系：パスワード不一致の場合、BadCredentialsExceptionをthrowする")
		void login_wrong_password() {
			assertThrows(BadCredentialsException.class,
				() -> authServiceImpl.login("testuser01", "wrongpassword"));
		}

		@Test
		@Order(4)
		@DisplayName("異常系：存在しないアカウントIDの場合、BadCredentialsExceptionをthrowする")
		void login_account_not_found() {
			assertThrows(BadCredentialsException.class,
				() -> authServiceImpl.login("notexists", TEST_PASSWORD));
		}

		@Test
		@Order(5)
		@DisplayName("異常系：アカウントロックの場合、LockedException をthrowする")
		void login_locked_account() {
			assertThrows(LockedException.class,
				() -> authServiceImpl.login("lockeduser", TEST_PASSWORD));
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
			AuthTokenModel loginResult = authServiceImpl.login("testuser01", TEST_PASSWORD);

			// リフレッシュ
			AuthTokenModel refreshResult = authServiceImpl.refresh(loginResult.getRefreshToken());

			assertNotNull(refreshResult.getAccessToken());
			assertFalse(refreshResult.getAccessToken().isEmpty());
			assertEquals(loginResult.getRefreshToken(), refreshResult.getRefreshToken());
			assertTrue(refreshResult.getExpiresIn() > 0);
		}

		@Test
		@Order(2)
		@DisplayName("異常系：存在しないリフレッシュトークンの場合、IllegalArgumentExceptionをthrowする")
		void refresh_invalid_token() {
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> authServiceImpl.refresh("invalid-refresh-token"));
			assertEquals("無効なリフレッシュトークンです", exception.getMessage());
		}

		@Test
		@Order(3)
		@DisplayName("異常系：無効化済みリフレッシュトークンの場合、IllegalArgumentExceptionをthrowする")
		void refresh_revoked_token() throws Exception {
			// ログインしてリフレッシュトークンを取得
			AuthTokenModel loginResult = authServiceImpl.login("testuser01", TEST_PASSWORD);
			String refreshToken = loginResult.getRefreshToken();

			// トークンを無効化
			refreshTokenRepository.revokeByTokenHash(hashToken(refreshToken));

			// 無効化済みトークンでリフレッシュ
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> authServiceImpl.refresh(refreshToken));
			assertEquals("無効なリフレッシュトークンです", exception.getMessage());
		}

		@Test
		@Order(4)
		@DisplayName("異常系：有効期限切れリフレッシュトークンの場合、IllegalArgumentExceptionをthrowする")
		void refresh_expired_token() throws Exception {
			// ログインしてリフレッシュトークンを取得
			AuthTokenModel loginResult = authServiceImpl.login("testuser01", TEST_PASSWORD);
			String refreshToken = loginResult.getRefreshToken();

			// 有効期限を過去に設定
			String tokenHash = hashToken(refreshToken);
			jdbcTemplate.update(
				"UPDATE common.refresh_token SET expires_at = ? WHERE token_hash = ?",
				OffsetDateTime.now().minusDays(1), tokenHash
			);

			// 有効期限切れトークンでリフレッシュ
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> authServiceImpl.refresh(refreshToken));
			assertEquals("リフレッシュトークンの有効期限が切れています", exception.getMessage());
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
			AuthTokenModel loginResult = authServiceImpl.login("testuser01", TEST_PASSWORD);
			String refreshToken = loginResult.getRefreshToken();
			String tokenHash = hashToken(refreshToken);

			// ログアウト前はトークンが有効
			RefreshTokenModel beforeLogout = refreshTokenRepository.findByTokenHash(tokenHash);
			assertNotNull(beforeLogout);
			assertFalse(beforeLogout.getIsRevoked());

			// ログアウト
			authServiceImpl.logout(refreshToken);

			// ログアウト後はトークンが無効化されている
			RefreshTokenModel afterLogout = refreshTokenRepository.findByTokenHash(tokenHash);
			assertNotNull(afterLogout);
			assertTrue(afterLogout.getIsRevoked());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：存在しないリフレッシュトークンでログアウトしてもエラーにならない")
		void logout_with_nonexistent_token() {
			assertDoesNotThrow(() -> authServiceImpl.logout("nonexistent-token"));
		}

		@Test
		@Order(3)
		@DisplayName("正常系：ログアウト後にリフレッシュが失敗する")
		void logout_then_refresh_fails() {
			// ログイン
			AuthTokenModel loginResult = authServiceImpl.login("testuser01", TEST_PASSWORD);
			String refreshToken = loginResult.getRefreshToken();

			// ログアウト
			authServiceImpl.logout(refreshToken);

			// ログアウト後のリフレッシュは失敗
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> authServiceImpl.refresh(refreshToken));
			assertEquals("無効なリフレッシュトークンです", exception.getMessage());
		}
	}
}
