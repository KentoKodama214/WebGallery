package com.web.gallery.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.OffsetDateTime;

import com.web.gallery.constant.Consts;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.account.Password;
import com.web.gallery.domain.auth.RefreshTokenValue;
import com.web.gallery.domain.common.ExpiresAt;
import com.web.gallery.domain.common.TokenHash;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web.gallery.AccountPrincipal;
import com.web.gallery.config.JwtConfig;
import com.web.gallery.exception.InvalidRefreshTokenException;
import com.web.gallery.helper.JwtTokenProvider;
import com.web.gallery.model.AccountModel;
import com.web.gallery.model.AuthTokenModel;
import com.web.gallery.model.RefreshTokenModel;
import com.web.gallery.repository.AccountRepository;
import com.web.gallery.repository.RefreshTokenRepository;
import com.web.gallery.service.AuthService;


/**
 * JWT認証に関するビジネスロジックを行うServiceの実装クラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
@Service
public class AuthServiceImpl implements AuthService {
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;
	private final JwtConfig jwtConfig;
	private final RefreshTokenRepository refreshTokenRepository;
	private final AccountRepository accountRepository;
	private final UserDetailsService userDetailsService;
	private final Clock clock;

	public AuthServiceImpl(
			@Lazy AuthenticationManager authenticationManager,
			JwtTokenProvider jwtTokenProvider,
			JwtConfig jwtConfig,
			RefreshTokenRepository refreshTokenRepository,
			AccountRepository accountRepository,
			@Lazy UserDetailsService userDetailsService,
			Clock clock) {
		this.authenticationManager = authenticationManager;
		this.jwtTokenProvider = jwtTokenProvider;
		this.jwtConfig = jwtConfig;
		this.refreshTokenRepository = refreshTokenRepository;
		this.accountRepository = accountRepository;
		this.userDetailsService = userDetailsService;
		this.clock = clock;
	}

	/**
	 * ログイン認証を行い、トークンを発行する
	 *
	 * @param	accountId	アカウントID
	 * @param	password	パスワード
	 * @return				{@link AuthTokenModel}
	 */
	@Override
	@Transactional
	public AuthTokenModel login(AccountId accountId, Password password) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(accountId.value(), password.value()));

		AccountPrincipal principal = (AccountPrincipal) authentication.getPrincipal();

		// 既存のリフレッシュトークンをすべて無効化（同時セッション制限）
		refreshTokenRepository.revokeAllByAccountNo(new AccountNo(principal.getAccountNo()));

		// 新しいトークンを生成
		String accessToken = jwtTokenProvider.generateAccessToken(principal);
		String refreshToken = jwtTokenProvider.generateRefreshToken();

		// リフレッシュトークンをDB保存（ハッシュ化して保存）
		refreshTokenRepository.save(RefreshTokenModel.of(
				new AccountNo(principal.getAccountNo()),
				new TokenHash(hashToken(refreshToken)),
				new ExpiresAt(OffsetDateTime.now(clock).plusDays(jwtConfig.getRefreshTokenExpirationDays()))));

		return AuthTokenModel.of(accessToken, refreshToken,
				(long) jwtConfig.getAccessTokenExpirationMinutes() * 60);
	}

	/**
	 * リフレッシュトークンを検証し、新しいアクセストークンを発行する
	 *
	 * @param	refreshToken	リフレッシュトークン
	 * @return					{@link AuthTokenModel}
	 * @throws	InvalidRefreshTokenException	トークンが無効な場合
	 * @throws	LockedException					アカウントがロックされている場合
	 */
	@Override
	@Transactional(readOnly = true)
	public AuthTokenModel refresh(RefreshTokenValue refreshToken) {
		TokenHash tokenHash = new TokenHash(hashToken(refreshToken.value()));
		RefreshTokenModel storedToken = refreshTokenRepository.findByTokenHash(tokenHash);

		if (storedToken == null || storedToken.getIsRevoked().value()) {
			throw new InvalidRefreshTokenException("無効なリフレッシュトークンです");
		}

		if (storedToken.getExpiresAt().value().isBefore(OffsetDateTime.now(clock))) {
			throw new InvalidRefreshTokenException("リフレッシュトークンの有効期限が切れています");
		}

		// アカウント番号からアカウント情報を取得し、新しいアクセストークンを発行
		AccountModel accountModel = accountRepository.getByAccountNo(storedToken.getAccountNo());
		if (accountModel == null) {
			throw new InvalidRefreshTokenException("無効なリフレッシュトークンです");
		}
		UserDetails userDetails = userDetailsService.loadUserByUsername(accountModel.getAccountId().value());
		AccountPrincipal principal = (AccountPrincipal) userDetails;

		if (!principal.isAccountNonLocked()) {
			throw new LockedException("アカウントがロックされています");
		}
		if (!principal.isEnabled()) {
			throw new InvalidRefreshTokenException("無効なリフレッシュトークンです");
		}

		String accessToken = jwtTokenProvider.generateAccessToken(principal);

		return AuthTokenModel.of(accessToken, refreshToken.value(),
				(long) jwtConfig.getAccessTokenExpirationMinutes() * 60);
	}

	/**
	 * ログアウトし、リフレッシュトークンを無効化する
	 *
	 * @param	refreshToken	リフレッシュトークン
	 */
	@Override
	@Transactional
	public void logout(RefreshTokenValue refreshToken) {
		refreshTokenRepository.revokeByTokenHash(new TokenHash(hashToken(refreshToken.value())));
	}

	/**
	 * トークンをSHA-256でハッシュ化する
	 *
	 * @param	token	ハッシュ化対象のトークン文字列
	 * @return			ハッシュ化された文字列（16進数表記）
	 */
	private String hashToken(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance(Consts.SHA_256);
			byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
			StringBuilder hexString = new StringBuilder();
			for (byte b : hash) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256アルゴリズムが利用できません", e);
		}
	}
}
