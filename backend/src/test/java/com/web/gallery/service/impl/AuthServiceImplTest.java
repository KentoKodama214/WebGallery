package com.web.gallery.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.web.gallery.AccountPrincipal;
import com.web.gallery.config.JwtConfig;
import com.web.gallery.constant.Consts;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.account.Password;
import com.web.gallery.domain.auth.RefreshTokenValue;
import com.web.gallery.domain.common.ExpiresAt;
import com.web.gallery.domain.common.IsRevoked;
import com.web.gallery.domain.common.TokenHash;
import com.web.gallery.exception.InvalidRefreshTokenException;
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
	private UserDetailsService userDetailsService;

	@Mock
	private Clock clock;

	@BeforeEach
	void setUpClock() {
		lenient().when(clock.instant()).thenReturn(Instant.now());
		lenient().when(clock.getZone()).thenReturn(Consts.JST);
	}

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

			AuthTokenModel result = authServiceImpl.login(new AccountId(accountId), new Password(password));

			assertNotNull(result);
			assertEquals("access-token", result.getAccessToken().value());
			assertEquals("refresh-token", result.getRefreshToken().value());
			assertEquals(900L, result.getExpiresIn().value());

			verify(refreshTokenRepository).revokeAllByAccountNo(new AccountNo(1L));
			ArgumentCaptor<RefreshTokenModel> refreshTokenModelCaptor = ArgumentCaptor.forClass(RefreshTokenModel.class);
			verify(refreshTokenRepository).save(refreshTokenModelCaptor.capture());
			assertEquals(OffsetDateTime.now(clock).plusDays(7), refreshTokenModelCaptor.getValue().getExpiresAt().value());
		}

		@Test
		@DisplayName("異常系: パスワードが間違っている場合は例外がスローされること")
		void login_badCredentials() {
			when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
					.thenThrow(new BadCredentialsException("Bad credentials"));

			assertThrows(BadCredentialsException.class, () -> {
				authServiceImpl.login(new AccountId("testuser1"), new Password("wrongpassword"));
			});
		}

		@Test
		@DisplayName("異常系: アカウントがロックされている場合は例外がスローされること")
		void login_accountLocked() {
			when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
					.thenThrow(new LockedException("Account is locked"));

			assertThrows(LockedException.class, () -> {
				authServiceImpl.login(new AccountId("testuser1"), new Password("password1"));
			});
		}
	}

	@Nested
	@DisplayName("#refresh")
	class Refresh {

		@Test
		@DisplayName("正常系: リフレッシュトークンが有効な場合、新しいアクセストークンとリフレッシュトークンが返され、旧トークンが無効化されること")
		void refresh_success() {
			String refreshToken = "valid-refresh-token";
			RefreshTokenModel storedToken = RefreshTokenModel.builder()
					.accountNo(new AccountNo(1L))
					.tokenHash(new TokenHash("hashed-token"))
					.expiresAt(new ExpiresAt(OffsetDateTime.now().plusDays(7)))
					.isRevoked(new IsRevoked(false))
					.build();

			when(refreshTokenRepository.findByTokenHash(any(TokenHash.class))).thenReturn(storedToken);

			AccountModel account = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId("testuser1"))
					.build();
			when(accountRepository.getByAccountNo(new AccountNo(1L))).thenReturn(account);

			AccountPrincipal principal = mock(AccountPrincipal.class);
			when(principal.isAccountNonLocked()).thenReturn(true);
			when(principal.isEnabled()).thenReturn(true);
			when(userDetailsService.loadUserByUsername("testuser1")).thenReturn(principal);
			when(jwtTokenProvider.generateAccessToken(principal)).thenReturn("new-access-token");
			when(jwtTokenProvider.generateRefreshToken()).thenReturn("new-refresh-token");
			when(jwtConfig.getAccessTokenExpirationMinutes()).thenReturn(15);
			when(jwtConfig.getRefreshTokenExpirationDays()).thenReturn(7);

			AuthTokenModel result = authServiceImpl.refresh(new RefreshTokenValue(refreshToken));

			assertNotNull(result);
			assertEquals("new-access-token", result.getAccessToken().value());
			assertEquals("new-refresh-token", result.getRefreshToken().value());
			assertEquals(900L, result.getExpiresIn().value());

			verify(refreshTokenRepository).revokeByTokenHash(any(TokenHash.class));
			verify(refreshTokenRepository, times(0)).revokeAllByAccountNo(any(AccountNo.class));

			ArgumentCaptor<RefreshTokenModel> refreshTokenModelCaptor = ArgumentCaptor.forClass(RefreshTokenModel.class);
			verify(refreshTokenRepository).save(refreshTokenModelCaptor.capture());
			assertEquals(new AccountNo(1L), refreshTokenModelCaptor.getValue().getAccountNo());
			assertEquals(OffsetDateTime.now(clock).plusDays(7), refreshTokenModelCaptor.getValue().getExpiresAt().value());
		}

		@Test
		@DisplayName("異常系: アカウントがロックされている場合は例外がスローされること")
		void refresh_accountLocked() {
			String refreshToken = "valid-refresh-token";
			RefreshTokenModel storedToken = RefreshTokenModel.builder()
					.accountNo(new AccountNo(1L))
					.tokenHash(new TokenHash("hashed-token"))
					.expiresAt(new ExpiresAt(OffsetDateTime.now().plusDays(7)))
					.isRevoked(new IsRevoked(false))
					.build();

			when(refreshTokenRepository.findByTokenHash(any(TokenHash.class))).thenReturn(storedToken);

			AccountModel account = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId("testuser1"))
					.build();
			when(accountRepository.getByAccountNo(new AccountNo(1L))).thenReturn(account);

			AccountPrincipal principal = mock(AccountPrincipal.class);
			when(principal.isAccountNonLocked()).thenReturn(false);
			when(userDetailsService.loadUserByUsername("testuser1")).thenReturn(principal);

			assertThrows(LockedException.class, () -> {
				authServiceImpl.refresh(new RefreshTokenValue(refreshToken));
			});
		}

		@Test
		@DisplayName("異常系: アカウントが無効化（削除済み）されている場合は例外がスローされること")
		void refresh_accountDisabled() {
			String refreshToken = "valid-refresh-token";
			RefreshTokenModel storedToken = RefreshTokenModel.builder()
					.accountNo(new AccountNo(1L))
					.tokenHash(new TokenHash("hashed-token"))
					.expiresAt(new ExpiresAt(OffsetDateTime.now().plusDays(7)))
					.isRevoked(new IsRevoked(false))
					.build();

			when(refreshTokenRepository.findByTokenHash(any(TokenHash.class))).thenReturn(storedToken);

			AccountModel account = AccountModel.builder()
					.accountNo(new AccountNo(1L))
					.accountId(new AccountId("testuser1"))
					.build();
			when(accountRepository.getByAccountNo(new AccountNo(1L))).thenReturn(account);

			AccountPrincipal principal = mock(AccountPrincipal.class);
			when(principal.isAccountNonLocked()).thenReturn(true);
			when(principal.isEnabled()).thenReturn(false);
			when(userDetailsService.loadUserByUsername("testuser1")).thenReturn(principal);

			assertThrows(InvalidRefreshTokenException.class, () -> {
				authServiceImpl.refresh(new RefreshTokenValue(refreshToken));
			});
		}

		@Test
		@DisplayName("異常系: 無効化済み（ローテーション済み）トークンが再利用された場合、盗用とみなし該当アカウントの全トークンを失効させたうえで例外がスローされること")
		void refresh_revokedToken() {
			RefreshTokenModel storedToken = RefreshTokenModel.builder()
					.accountNo(new AccountNo(1L))
					.tokenHash(new TokenHash("hashed-token"))
					.expiresAt(new ExpiresAt(OffsetDateTime.now().plusDays(7)))
					.isRevoked(new IsRevoked(true))
					.build();

			when(refreshTokenRepository.findByTokenHash(any(TokenHash.class))).thenReturn(storedToken);

			assertThrows(InvalidRefreshTokenException.class, () -> {
				authServiceImpl.refresh(new RefreshTokenValue("revoked-token"));
			});

			verify(refreshTokenRepository).revokeAllByAccountNo(new AccountNo(1L));
			verify(refreshTokenRepository, times(0)).save(any(RefreshTokenModel.class));
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

			when(refreshTokenRepository.findByTokenHash(any(TokenHash.class))).thenReturn(storedToken);

			assertThrows(InvalidRefreshTokenException.class, () -> {
				authServiceImpl.refresh(new RefreshTokenValue("expired-token"));
			});
		}

		@Test
		@DisplayName("異常系: リフレッシュトークンが存在しない場合は例外がスローされること")
		void refresh_tokenNotFound() {
			when(refreshTokenRepository.findByTokenHash(any(TokenHash.class))).thenReturn(null);

			assertThrows(InvalidRefreshTokenException.class, () -> {
				authServiceImpl.refresh(new RefreshTokenValue("nonexistent-token"));
			});
		}

		@Test
		@DisplayName("異常系: トークンに紐づくアカウントが既に削除されている場合は例外がスローされること")
		void refresh_accountNotFound() {
			RefreshTokenModel storedToken = RefreshTokenModel.builder()
					.accountNo(new AccountNo(1L))
					.tokenHash(new TokenHash("hashed-token"))
					.expiresAt(new ExpiresAt(OffsetDateTime.now().plusDays(7)))
					.isRevoked(new IsRevoked(false))
					.build();

			when(refreshTokenRepository.findByTokenHash(any(TokenHash.class))).thenReturn(storedToken);
			when(accountRepository.getByAccountNo(new AccountNo(1L))).thenReturn(null);

			assertThrows(InvalidRefreshTokenException.class, () -> {
				authServiceImpl.refresh(new RefreshTokenValue("valid-refresh-token"));
			});
		}
	}

	@Nested
	@DisplayName("#logout")
	class Logout {

		@Test
		@DisplayName("正常系: リフレッシュトークンが無効化されること")
		void logout_success() {
			authServiceImpl.logout(new RefreshTokenValue("refresh-token"));

			verify(refreshTokenRepository).revokeByTokenHash(any(TokenHash.class));
		}
	}
}
