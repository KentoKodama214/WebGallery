package com.web.gallery.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

import java.time.OffsetDateTime;
import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.web.gallery.AccountPrincipal;
import com.web.gallery.config.JwtConfig;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.ExpiresAt;
import com.web.gallery.domain.common.IsRevoked;
import com.web.gallery.domain.common.TokenHash;
import com.web.gallery.helper.JwtTokenProvider;
import com.web.gallery.model.AccountModel;
import com.web.gallery.model.AuthTokenModel;
import com.web.gallery.model.RefreshTokenModel;
import com.web.gallery.repository.AccountRepository;
import com.web.gallery.repository.RefreshTokenRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

	@InjectMocks
	private AuthServiceImpl authServiceImpl;

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private JwtConfig jwtConfig;

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private AccountServiceImpl accountServiceImpl;

	@Nested
	@DisplayName("#login")
	class Login {

		@Test
		@DisplayName("正常系: ログインに成功し、トークンが返されること")
		void login_success() {
			String accountId = "testuser1";
			String password = "password1";

			AccountPrincipal principal = mock(AccountPrincipal.class);
			when(principal.getAccountNo()).thenReturn(1L);

			Authentication authentication = mock(Authentication.class);
			when(authentication.getPrincipal()).thenReturn(principal);
			when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
					.thenReturn(authentication);

			when(jwtTokenProvider.generateAccessToken(principal)).thenReturn("access-token");
			when(jwtTokenProvider.generateRefreshToken()).thenReturn("refresh-token");
			when(jwtConfig.getRefreshTokenExpirationDays()).thenReturn(7);
			when(jwtConfig.getAccessTokenExpirationMinutes()).thenReturn(15);

			AuthTokenModel result = authServiceImpl.login(accountId, password);

			assertNotNull(result);
			assertEquals("access-token", result.getAccessToken().value());
			assertEquals("refresh-token", result.getRefreshToken().value());
			assertEquals(900L, result.getExpiresIn().value());

			verify(refreshTokenRepository).revokeAllByAccountNo(1L);
			verify(refreshTokenRepository).save(any(RefreshTokenModel.class));
		}

		@Test
		@DisplayName("異常系: パスワードが間違っている場合は例外がスローされること")
		void login_badCredentials() {
			when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
					.thenThrow(new BadCredentialsException("Bad credentials"));

			assertThrows(BadCredentialsException.class, () -> {
				authServiceImpl.login("testuser1", "wrongpassword");
			});
		}

		@Test
		@DisplayName("異常系: アカウントがロックされている場合は例外がスローされること")
		void login_accountLocked() {
			when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
					.thenThrow(new LockedException("Account is locked"));

			assertThrows(LockedException.class, () -> {
				authServiceImpl.login("testuser1", "password1");
			});
		}
	}

	@Nested
	@DisplayName("#refresh")
	class Refresh {

		@Test
		@DisplayName("正常系: リフレッシュトークンが有効な場合、新しいアクセストークンが返されること")
		void refresh_success() {
			String refreshToken = "valid-refresh-token";
			RefreshTokenModel storedToken = RefreshTokenModel.builder()
					.accountNo(new AccountNo(1L))
					.tokenHash(new TokenHash("hashed-token"))
					.expiresAt(new ExpiresAt(OffsetDateTime.now().plusDays(7)))
					.isRevoked(new IsRevoked(false))
					.build();

			when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(storedToken);

			AccountModel account = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId("testuser1"))
					.build();
			when(accountRepository.getByAccountNo(1L)).thenReturn(account);

			AccountPrincipal principal = mock(AccountPrincipal.class);
			when(accountServiceImpl.loadUserByUsername("testuser1")).thenReturn(principal);
			when(jwtTokenProvider.generateAccessToken(principal)).thenReturn("new-access-token");
			when(jwtConfig.getAccessTokenExpirationMinutes()).thenReturn(15);

			AuthTokenModel result = authServiceImpl.refresh(refreshToken);

			assertNotNull(result);
			assertEquals("new-access-token", result.getAccessToken().value());
			assertEquals(900L, result.getExpiresIn().value());
		}

		@Test
		@DisplayName("異常系: リフレッシュトークンが無効化されている場合は例外がスローされること")
		void refresh_revokedToken() {
			RefreshTokenModel storedToken = RefreshTokenModel.builder()
					.accountNo(new AccountNo(1L))
					.tokenHash(new TokenHash("hashed-token"))
					.expiresAt(new ExpiresAt(OffsetDateTime.now().plusDays(7)))
					.isRevoked(new IsRevoked(true))
					.build();

			when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(storedToken);

			assertThrows(IllegalArgumentException.class, () -> {
				authServiceImpl.refresh("revoked-token");
			});
		}

		@Test
		@DisplayName("異常系: リフレッシュトークンの有効期限が切れている場合は例外がスローされること")
		void refresh_expiredToken() {
			RefreshTokenModel storedToken = RefreshTokenModel.builder()
					.accountNo(new AccountNo(1L))
					.tokenHash(new TokenHash("hashed-token"))
					.expiresAt(new ExpiresAt(OffsetDateTime.now().minusDays(1)))
					.isRevoked(new IsRevoked(false))
					.build();

			when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(storedToken);

			assertThrows(IllegalArgumentException.class, () -> {
				authServiceImpl.refresh("expired-token");
			});
		}

		@Test
		@DisplayName("異常系: リフレッシュトークンが存在しない場合は例外がスローされること")
		void refresh_tokenNotFound() {
			when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(null);

			assertThrows(IllegalArgumentException.class, () -> {
				authServiceImpl.refresh("nonexistent-token");
			});
		}
	}

	@Nested
	@DisplayName("#logout")
	class Logout {

		@Test
		@DisplayName("正常系: リフレッシュトークンが無効化されること")
		void logout_success() {
			authServiceImpl.logout("refresh-token");

			verify(refreshTokenRepository).revokeByTokenHash(anyString());
		}
	}
}
