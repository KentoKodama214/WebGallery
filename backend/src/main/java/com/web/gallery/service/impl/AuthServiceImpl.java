package com.web.gallery.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;

import com.web.gallery.constant.Consts;
import com.web.gallery.domain.account.AccountNo;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web.gallery.AccountPrincipal;
import com.web.gallery.config.JwtConfig;
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
	private final AccountServiceImpl accountServiceImpl;

	public AuthServiceImpl(
			@Lazy AuthenticationManager authenticationManager,
			JwtTokenProvider jwtTokenProvider,
			JwtConfig jwtConfig,
			RefreshTokenRepository refreshTokenRepository,
			AccountRepository accountRepository,
			@Lazy AccountServiceImpl accountServiceImpl) {
		this.authenticationManager = authenticationManager;
		this.jwtTokenProvider = jwtTokenProvider;
		this.jwtConfig = jwtConfig;
		this.refreshTokenRepository = refreshTokenRepository;
		this.accountRepository = accountRepository;
		this.accountServiceImpl = accountServiceImpl;
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
	public AuthTokenModel login(String accountId, String password) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(accountId, password));

		AccountPrincipal principal = (AccountPrincipal) authentication.getPrincipal();

		// 既存のリフレッシュトークンをすべて無効化（同時セッション制限）
		refreshTokenRepository.revokeAllByAccountNo(principal.getAccountNo());

		// 新しいトークンを生成
		String accessToken = jwtTokenProvider.generateAccessToken(principal);
		String refreshToken = jwtTokenProvider.generateRefreshToken();

		// リフレッシュトークンをDB保存（ハッシュ化して保存）
		RefreshTokenModel refreshTokenModel = RefreshTokenModel.builder()
				.accountNo(new AccountNo(principal.getAccountNo()))
				.tokenHash(hashToken(refreshToken))
				.expiresAt(OffsetDateTime.now().plusDays(jwtConfig.getRefreshTokenExpirationDays()))
				.build();
		refreshTokenRepository.save(refreshTokenModel);

		return AuthTokenModel.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.expiresIn((long) jwtConfig.getAccessTokenExpirationMinutes() * 60)
				.build();
	}

	/**
	 * リフレッシュトークンを検証し、新しいアクセストークンを発行する
	 *
	 * @param	refreshToken	リフレッシュトークン
	 * @return					{@link AuthTokenModel}
	 * @throws	IllegalArgumentException	トークンが無効な場合
	 */
	@Override
	@Transactional(readOnly = true)
	public AuthTokenModel refresh(String refreshToken) {
		String tokenHash = hashToken(refreshToken);
		RefreshTokenModel storedToken = refreshTokenRepository.findByTokenHash(tokenHash);

		if (storedToken == null || storedToken.getIsRevoked()) {
			throw new IllegalArgumentException("無効なリフレッシュトークンです");
		}

		if (storedToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
			throw new IllegalArgumentException("リフレッシュトークンの有効期限が切れています");
		}

		// アカウント番号からアカウント情報を取得し、新しいアクセストークンを発行
		AccountModel accountModel = accountRepository.getByAccountNo(storedToken.getAccountNo().getValue());
		UserDetails userDetails = accountServiceImpl.loadUserByUsername(accountModel.getAccountId().getValue());
		AccountPrincipal principal = (AccountPrincipal) userDetails;

		String accessToken = jwtTokenProvider.generateAccessToken(principal);

		return AuthTokenModel.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.expiresIn((long) jwtConfig.getAccessTokenExpirationMinutes() * 60)
				.build();
	}

	/**
	 * ログアウトし、リフレッシュトークンを無効化する
	 *
	 * @param	refreshToken	リフレッシュトークン
	 */
	@Override
	@Transactional
	public void logout(String refreshToken) {
		refreshTokenRepository.revokeByTokenHash(hashToken(refreshToken));
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
