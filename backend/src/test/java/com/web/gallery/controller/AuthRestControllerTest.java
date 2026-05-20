package com.web.gallery.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.web.gallery.config.JwtConfig;
import com.web.gallery.model.AuthTokenModel;
import com.web.gallery.service.impl.AuthServiceImpl;

import jakarta.servlet.http.Cookie;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AuthRestControllerTest {
	@InjectMocks
	private AuthRestController authRestController;

	@Mock
	private AuthServiceImpl authServiceImpl;

	@Mock
	private JwtConfig jwtConfig;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(authRestController).build();
	}

	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class login {
		@Test
		@Order(1)
		@DisplayName("正常系：ログイン成功")
		void login_success() throws Exception {
			AuthTokenModel tokenModel = AuthTokenModel.builder()
					.accessToken("test-access-token")
					.refreshToken("test-refresh-token")
					.expiresIn(900L)
					.build();

			doReturn(tokenModel).when(authServiceImpl).login("testuser", "password123");
			doReturn(7).when(jwtConfig).getRefreshTokenExpirationDays();

			mockMvc.perform(
					post("/api/v1/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"accountId\":\"testuser\",\"password\":\"password123\"}")
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value("test-access-token"))
				.andExpect(jsonPath("$.expiresIn").value(900))
				.andExpect(header().exists("Set-Cookie"));

			verify(authServiceImpl, times(1)).login("testuser", "password123");
		}

		@Test
		@Order(2)
		@DisplayName("異常系：accountIdが空の場合、BadRequestExceptionをthrowする")
		void login_accountId_blank() throws Exception {
			mockMvc.perform(
					post("/api/v1/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"accountId\":\"\",\"password\":\"password123\"}")
				)
				.andExpect(status().isBadRequest());

			verify(authServiceImpl, times(0)).login(anyString(), anyString());
		}

		@Test
		@Order(3)
		@DisplayName("異常系：passwordが空の場合、BadRequestExceptionをthrowする")
		void login_password_blank() throws Exception {
			mockMvc.perform(
					post("/api/v1/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"accountId\":\"testuser\",\"password\":\"\"}")
				)
				.andExpect(status().isBadRequest());

			verify(authServiceImpl, times(0)).login(anyString(), anyString());
		}

		@Test
		@Order(4)
		@DisplayName("異常系：認証失敗（BadCredentialsException）の場合、401を返す")
		void login_bad_credentials() throws Exception {
			doThrow(new BadCredentialsException("Bad credentials"))
				.when(authServiceImpl).login("testuser", "wrongpassword");

			mockMvc.perform(
					post("/api/v1/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"accountId\":\"testuser\",\"password\":\"wrongpassword\"}")
				)
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("アカウントIDまたはパスワードが間違っています。"));
		}

		@Test
		@Order(5)
		@DisplayName("異常系：アカウントロック（LockedException）の場合、423を返す")
		void login_locked() throws Exception {
			doThrow(new LockedException("Account is locked"))
				.when(authServiceImpl).login("testuser", "password123");

			mockMvc.perform(
					post("/api/v1/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"accountId\":\"testuser\",\"password\":\"password123\"}")
				)
				.andExpect(status().is(423))
				.andExpect(jsonPath("$.message").value("アカウントがロックされています。"));
		}
	}

	@Nested
	@Order(2)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class refresh {
		@Test
		@Order(1)
		@DisplayName("正常系：リフレッシュ成功")
		void refresh_success() throws Exception {
			AuthTokenModel tokenModel = AuthTokenModel.builder()
					.accessToken("new-access-token")
					.refreshToken("test-refresh-token")
					.expiresIn(900L)
					.build();

			doReturn(tokenModel).when(authServiceImpl).refresh("test-refresh-token");

			mockMvc.perform(
					post("/api/v1/auth/refresh")
					.cookie(new Cookie("refreshToken", "test-refresh-token"))
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value("new-access-token"))
				.andExpect(jsonPath("$.expiresIn").value(900));

			verify(authServiceImpl, times(1)).refresh("test-refresh-token");
		}

		@Test
		@Order(2)
		@DisplayName("異常系：refreshTokenがnullの場合、401を返す")
		void refresh_no_cookie() throws Exception {
			mockMvc.perform(
					post("/api/v1/auth/refresh")
				)
				.andExpect(status().isUnauthorized());

			verify(authServiceImpl, times(0)).refresh(anyString());
		}

		@Test
		@Order(3)
		@DisplayName("異常系：refreshTokenが空の場合、401を返す")
		void refresh_empty_cookie() throws Exception {
			mockMvc.perform(
					post("/api/v1/auth/refresh")
					.cookie(new Cookie("refreshToken", ""))
				)
				.andExpect(status().isUnauthorized());

			verify(authServiceImpl, times(0)).refresh(anyString());
		}

		@Test
		@Order(4)
		@DisplayName("異常系：無効なリフレッシュトークンの場合、401を返す")
		void refresh_invalid_token() throws Exception {
			doThrow(new IllegalArgumentException("無効なリフレッシュトークンです"))
				.when(authServiceImpl).refresh("invalid-token");

			mockMvc.perform(
					post("/api/v1/auth/refresh")
					.cookie(new Cookie("refreshToken", "invalid-token"))
				)
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("無効なリフレッシュトークンです"));
		}
	}

	@Nested
	@Order(3)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class logout {
		@Test
		@Order(1)
		@DisplayName("正常系：ログアウト成功（refreshTokenあり）")
		void logout_with_token() throws Exception {
			doNothing().when(authServiceImpl).logout("test-refresh-token");

			mockMvc.perform(
					post("/api/v1/auth/logout")
					.cookie(new Cookie("refreshToken", "test-refresh-token"))
				)
				.andExpect(status().isNoContent())
				.andExpect(header().exists("Set-Cookie"));

			verify(authServiceImpl, times(1)).logout("test-refresh-token");
		}

		@Test
		@Order(2)
		@DisplayName("正常系：ログアウト成功（refreshTokenなし）")
		void logout_without_token() throws Exception {
			mockMvc.perform(
					post("/api/v1/auth/logout")
				)
				.andExpect(status().isNoContent())
				.andExpect(header().exists("Set-Cookie"));

			verify(authServiceImpl, times(0)).logout(anyString());
		}

		@Test
		@Order(3)
		@DisplayName("正常系：ログアウト成功（refreshTokenが空）")
		void logout_empty_token() throws Exception {
			mockMvc.perform(
					post("/api/v1/auth/logout")
					.cookie(new Cookie("refreshToken", ""))
				)
				.andExpect(status().isNoContent())
				.andExpect(header().exists("Set-Cookie"));

			verify(authServiceImpl, times(0)).logout(anyString());
		}
	}
}
