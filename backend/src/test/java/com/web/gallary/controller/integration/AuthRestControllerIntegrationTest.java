package com.web.gallary.controller.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class AuthRestControllerIntegrationTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private static final String TEST_PASSWORD = "password123";

	/**
	 * テストデータ投入（BCryptハッシュはテスト実行時に動的生成）
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
			MvcResult result = mockMvc.perform(
					post("/api/v1/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"accountId\":\"testuser01\",\"password\":\"" + TEST_PASSWORD + "\"}")
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.expiresIn").isNumber())
				.andExpect(header().exists("Set-Cookie"))
				.andReturn();

			// Set-CookieヘッダーにrefreshTokenが含まれていることを検証
			String setCookie = result.getResponse().getHeader("Set-Cookie");
			assertNotNull(setCookie);
			assertTrue(setCookie.contains("refreshToken="));
			assertTrue(setCookie.contains("HttpOnly"));
			assertTrue(setCookie.contains("Secure"));
			assertTrue(setCookie.contains("Path=/api/v1/auth"));
		}

		@Test
		@Order(2)
		@DisplayName("異常系：パスワード不一致の場合、401を返す")
		void login_wrong_password() throws Exception {
			mockMvc.perform(
					post("/api/v1/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"accountId\":\"testuser01\",\"password\":\"wrongpassword\"}")
				)
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("アカウントIDまたはパスワードが間違っています。"));
		}

		@Test
		@Order(3)
		@DisplayName("異常系：存在しないアカウントIDの場合、401を返す")
		void login_account_not_found() throws Exception {
			mockMvc.perform(
					post("/api/v1/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"accountId\":\"notexists\",\"password\":\"" + TEST_PASSWORD + "\"}")
				)
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("アカウントIDまたはパスワードが間違っています。"));
		}

		@Test
		@Order(4)
		@DisplayName("異常系：アカウントロックの場合、423を返す")
		void login_locked_account() throws Exception {
			mockMvc.perform(
					post("/api/v1/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"accountId\":\"lockeduser\",\"password\":\"" + TEST_PASSWORD + "\"}")
				)
				.andExpect(status().is(423))
				.andExpect(jsonPath("$.message").value("アカウントがロックされています。"));
		}

		@Test
		@Order(5)
		@DisplayName("異常系：accountIdが空の場合、400を返す")
		void login_blank_accountId() throws Exception {
			mockMvc.perform(
					post("/api/v1/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"accountId\":\"\",\"password\":\"" + TEST_PASSWORD + "\"}")
				)
				.andExpect(status().isBadRequest());
		}

		@Test
		@Order(6)
		@DisplayName("異常系：passwordが空の場合、400を返す")
		void login_blank_password() throws Exception {
			mockMvc.perform(
					post("/api/v1/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"accountId\":\"testuser01\",\"password\":\"\"}")
				)
				.andExpect(status().isBadRequest());
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
		void refresh_success() throws Exception {
			// まずログインしてリフレッシュトークンを取得
			MvcResult loginResult = mockMvc.perform(
					post("/api/v1/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"accountId\":\"testuser01\",\"password\":\"" + TEST_PASSWORD + "\"}")
				)
				.andExpect(status().isOk())
				.andReturn();

			Cookie refreshTokenCookie = loginResult.getResponse().getCookie("refreshToken");
			assertNotNull(refreshTokenCookie);

			// リフレッシュトークンを使ってアクセストークンを更新
			mockMvc.perform(
					post("/api/v1/auth/refresh")
					.cookie(refreshTokenCookie)
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.expiresIn").isNumber());
		}

		@Test
		@Order(2)
		@DisplayName("異常系：refreshTokenがない場合、401を返す")
		void refresh_no_cookie() throws Exception {
			mockMvc.perform(
					post("/api/v1/auth/refresh")
				)
				.andExpect(status().isUnauthorized());
		}

		@Test
		@Order(3)
		@DisplayName("異常系：無効なrefreshTokenの場合、401を返す")
		void refresh_invalid_token() throws Exception {
			mockMvc.perform(
					post("/api/v1/auth/refresh")
					.cookie(new Cookie("refreshToken", "invalid-refresh-token"))
				)
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("無効なリフレッシュトークンです"));
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
		@DisplayName("正常系：ログアウト成功（refreshTokenあり）")
		void logout_with_token() throws Exception {
			// まずログインしてリフレッシュトークンを取得
			MvcResult loginResult = mockMvc.perform(
					post("/api/v1/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"accountId\":\"testuser01\",\"password\":\"" + TEST_PASSWORD + "\"}")
				)
				.andExpect(status().isOk())
				.andReturn();

			Cookie refreshTokenCookie = loginResult.getResponse().getCookie("refreshToken");
			assertNotNull(refreshTokenCookie);

			// ログアウト
			MvcResult logoutResult = mockMvc.perform(
					post("/api/v1/auth/logout")
					.cookie(refreshTokenCookie)
				)
				.andExpect(status().isNoContent())
				.andExpect(header().exists("Set-Cookie"))
				.andReturn();

			// Set-CookieでrefreshTokenがクリアされていることを検証
			String setCookie = logoutResult.getResponse().getHeader("Set-Cookie");
			assertNotNull(setCookie);
			assertTrue(setCookie.contains("refreshToken="));
			assertTrue(setCookie.contains("Max-Age=0"));

			// ログアウト後、同じリフレッシュトークンでリフレッシュが失敗することを検証
			mockMvc.perform(
					post("/api/v1/auth/refresh")
					.cookie(refreshTokenCookie)
				)
				.andExpect(status().isUnauthorized());
		}

		@Test
		@Order(2)
		@DisplayName("正常系：ログアウト成功（refreshTokenなし）")
		void logout_without_token() throws Exception {
			MvcResult result = mockMvc.perform(
					post("/api/v1/auth/logout")
				)
				.andExpect(status().isNoContent())
				.andExpect(header().exists("Set-Cookie"))
				.andReturn();

			String setCookie = result.getResponse().getHeader("Set-Cookie");
			assertNotNull(setCookie);
			assertTrue(setCookie.contains("Max-Age=0"));
		}
	}
}
