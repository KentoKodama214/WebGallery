package com.web.gallery.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.OffsetDateTime;

import com.web.gallery.constant.Consts;
import com.web.gallery.constant.MessageConst;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web.gallery.AccountPrincipal;
import com.web.gallery.config.JwtConfig;
import com.web.gallery.config.LoginConfig;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.exception.InvalidRefreshTokenException;
import com.web.gallery.helper.JwtTokenProvider;
import com.web.gallery.model.AccountModel;
import com.web.gallery.model.AuthTokenModel;
import com.web.gallery.model.RefreshTokenModel;
import com.web.gallery.repository.AccountRepository;
import com.web.gallery.repository.RefreshTokenRepository;
import com.web.gallery.service.AuthService;

import lombok.extern.slf4j.Slf4j;


/**
 * JWT認証に関するビジネスロジックを行うServiceの実装クラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;
	private final JwtConfig jwtConfig;
	private final LoginConfig loginConfig;
	private final RefreshTokenRepository refreshTokenRepository;
	private final AccountRepository accountRepository;
	private final Clock clock;

	public AuthServiceImpl(
			@Lazy AuthenticationManager authenticationManager,
			JwtTokenProvider jwtTokenProvider,
			JwtConfig jwtConfig,
			LoginConfig loginConfig,
			RefreshTokenRepository refreshTokenRepository,
			AccountRepository accountRepository,
			Clock clock) {
		this.authenticationManager = authenticationManager;
		this.jwtTokenProvider = jwtTokenProvider;
		this.jwtConfig = jwtConfig;
		this.loginConfig = loginConfig;
		this.refreshTokenRepository = refreshTokenRepository;
		this.accountRepository = accountRepository;
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
		// 同一アカウントIDへのログイン試行をDBレベルで直列化し、
		// 「ロックアウト判定 → 失敗回数加算」の間の競合による失敗回数上限のバイパスを防ぐ
		accountRepository.lockForLoginAttempt(accountId);

		AccountModel lockCheckModel = accountRepository.getByAccountId(accountId);
		if (isLocked(lockCheckModel)) {
			if (isLockDurationElapsed(lockCheckModel)) {
				// 最終失敗から一定時間が経過していればロックを自動解除する（総当たり攻撃中は失敗のたびに更新時刻が進むため解除されない）
				releaseLock(lockCheckModel);
			} else {
				throw new LockedException(MessageConst.ERR_ACCOUNT_LOCKED);
			}
		}

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
	 * リフレッシュトークンを検証し、新しいアクセストークンを発行する<p>
	 * リフレッシュのたびに新しいリフレッシュトークンを発行して旧トークンを無効化する（トークンローテーション）。
	 * 無効化済みトークンの再利用を検知した場合は盗用の疑いがあるため、該当アカウントの全トークンを失効させる
	 *
	 * @param	refreshToken	リフレッシュトークン
	 * @return					{@link AuthTokenModel}
	 * @throws	InvalidRefreshTokenException	トークンが無効な場合
	 * @throws	LockedException					アカウントがロックされている場合
	 */
	@Override
	@Transactional
	public AuthTokenModel refresh(RefreshTokenValue refreshToken) {
		TokenHash tokenHash = new TokenHash(hashToken(refreshToken.value()));
		// 同一リフレッシュトークンによる同時リクエストを行ロックで直列化し、
		// 「無効化チェック → ローテーション」間の競合による多重セッション発行を防ぐ
		RefreshTokenModel storedToken = refreshTokenRepository.findByTokenHashForUpdate(tokenHash);

		if (storedToken == null) {
			throw new InvalidRefreshTokenException(MessageConst.ERR_INVALID_REFRESH_TOKEN);
		}

		if (storedToken.getIsRevoked().value()) {
			if (isWithinReuseGracePeriod(storedToken)) {
				// ローテーション直後の猶予期間内の再送は、複数タブ・ネットワーク瞬断による
				// 正常系のリトライとみなす。全セッション失効はせず、このリクエストのみ拒否する
				// （正しい最新トークンは既にクライアントへ発行済みのため、次回リクエストで通る）
				log.info("Revoked refresh token reused within grace period. Treated as a benign retry. (accountNo: {})",
						storedToken.getAccountNo().value());
				throw new InvalidRefreshTokenException(MessageConst.ERR_INVALID_REFRESH_TOKEN);
			}
			// 猶予期間を超えた無効化済み（ローテーション済み）トークンの再利用は
			// 盗用の疑いがあるため、該当アカウントの全トークンを失効させる
			refreshTokenRepository.revokeAllByAccountNo(storedToken.getAccountNo());
			throw new InvalidRefreshTokenException(MessageConst.ERR_INVALID_REFRESH_TOKEN);
		}

		if (storedToken.getExpiresAt().value().isBefore(OffsetDateTime.now(clock))) {
			throw new InvalidRefreshTokenException(MessageConst.ERR_REFRESH_TOKEN_EXPIRED);
		}

		// アカウント番号からアカウント情報を取得し、新しいアクセストークンを発行
		AccountModel accountModel = accountRepository.getByAccountNo(storedToken.getAccountNo());
		if (accountModel == null) {
			throw new InvalidRefreshTokenException(MessageConst.ERR_INVALID_REFRESH_TOKEN);
		}
		// 取得済みのアカウント情報からそのままPrincipalを組み立てる（アカウントの二重取得を避ける）
		AccountPrincipal principal = new AccountPrincipal(accountModel, loginConfig.getFailCount());

		if (!principal.isAccountNonLocked()) {
			if (isLockDurationElapsed(accountModel)) {
				releaseLock(accountModel);
			} else {
				throw new LockedException(MessageConst.ERR_ACCOUNT_LOCKED);
			}
		}
		if (!principal.isEnabled()) {
			throw new InvalidRefreshTokenException(MessageConst.ERR_INVALID_REFRESH_TOKEN);
		}

		// リフレッシュトークンをローテーション（旧トークンを無効化し、新トークンを発行）
		refreshTokenRepository.revokeByTokenHash(tokenHash);
		String newRefreshToken = jwtTokenProvider.generateRefreshToken();
		refreshTokenRepository.save(RefreshTokenModel.of(
				storedToken.getAccountNo(),
				new TokenHash(hashToken(newRefreshToken)),
				new ExpiresAt(OffsetDateTime.now(clock).plusDays(jwtConfig.getRefreshTokenExpirationDays()))));

		String accessToken = jwtTokenProvider.generateAccessToken(principal);

		return AuthTokenModel.of(accessToken, newRefreshToken,
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
	 * 有効期限切れのリフレッシュトークンをDBから削除する
	 */
	@Override
	@Transactional
	public void purgeExpiredRefreshTokens() {
		refreshTokenRepository.deleteExpired();
	}

	/**
	 * アカウントがロック状態（管理者ロック、またはログイン失敗回数の上限到達）かどうかを判定する
	 *
	 * @param	accountModel	{@link AccountModel}（null可）
	 * @return					ロック状態の場合true
	 */
	private boolean isLocked(AccountModel accountModel) {
		if (accountModel == null) {
			return false;
		}
		return isAdminLocked(accountModel)
				|| (accountModel.getLoginFailureCount() != null
						&& accountModel.getLoginFailureCount().value() >= loginConfig.getFailCount());
	}

	/**
	 * アカウントが管理者により強制ロックされているかどうかを判定する
	 *
	 * @param	accountModel	{@link AccountModel}（null可）
	 * @return					管理者ロックされている場合true
	 */
	private boolean isAdminLocked(AccountModel accountModel) {
		return accountModel != null
				&& accountModel.getIsAdminLocked() != null
				&& Boolean.TRUE.equals(accountModel.getIsAdminLocked().value());
	}

	/**
	 * 無効化済みリフレッシュトークンの再送が、ローテーション直後の猶予期間内かどうかを判定する<p>
	 * {@code updated_at}（＝ローテーションで無効化した時刻）から
	 * {@code app.jwt.refreshTokenReuseGraceSeconds} 秒以内であれば正常系のリトライとみなす。
	 * 更新日時が取得できない場合は猶予対象外（盗用対応を優先）とする。
	 *
	 * @param	storedToken	DBから取得した{@link RefreshTokenModel}
	 * @return				猶予期間内の場合true
	 */
	private boolean isWithinReuseGracePeriod(RefreshTokenModel storedToken) {
		if (storedToken.getUpdatedAt() == null) {
			return false;
		}
		return storedToken.getUpdatedAt().value()
				.plusSeconds(jwtConfig.getRefreshTokenReuseGraceSeconds())
				.isAfter(OffsetDateTime.now(clock));
	}

	/**
	 * アカウントの最終更新（＝直近のログイン失敗）から、ロック自動解除までの時間が経過しているかどうかを判定する<p>
	 * 管理者による強制ロックは自動解除の対象外（管理者による解除のみで解ける）
	 *
	 * @param	accountModel	{@link AccountModel}（null可）
	 * @return					自動解除可能な場合true
	 */
	private boolean isLockDurationElapsed(AccountModel accountModel) {
		if (accountModel == null || accountModel.getUpdatedAt() == null || isAdminLocked(accountModel)) {
			return false;
		}
		return accountModel.getUpdatedAt().value()
				.plusMinutes(loginConfig.getLockDurationMinutes())
				.isBefore(OffsetDateTime.now(clock));
	}

	/**
	 * アカウントロックを解除する（ログイン失敗回数を0にリセットする）<p>
	 * 解除対象の行は直前に取得済みで必ず存在するため通常は失敗しないが、
	 * 万一失敗しても認証処理は継続する（次回試行で再度解除を試みる）
	 *
	 * @param	accountModel	{@link AccountModel}
	 */
	private void releaseLock(AccountModel accountModel) {
		try {
			accountRepository.updateLoginFailureCount(AccountModel.forUnlock(accountModel.getAccountNo().value()));
		} catch (GalleryException e) {
			log.warn("Failed to auto-release account lock. (accountNo: {})", accountModel.getAccountNo().value(), e);
		}
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
