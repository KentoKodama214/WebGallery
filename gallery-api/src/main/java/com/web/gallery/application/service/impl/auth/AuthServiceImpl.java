package com.web.gallery.application.service.impl.auth;

import com.web.gallery.application.AccountPrincipal;
import com.web.gallery.application.config.JwtConfig;
import com.web.gallery.application.config.LoginConfig;
import com.web.gallery.application.config.WebAuthnConfig;
import com.web.gallery.application.helper.JwtTokenProvider;
import com.web.gallery.application.helper.WebAuthnVerifier;
import com.web.gallery.application.model.account.AccountModel;
import com.web.gallery.application.model.auth.AuthTokenModel;
import com.web.gallery.application.model.auth.LoginContextModel;
import com.web.gallery.application.model.auth.PasskeyAssertionModel;
import com.web.gallery.application.model.auth.PasskeyAssertionResultModel;
import com.web.gallery.application.model.auth.PasskeyAttestationModel;
import com.web.gallery.application.model.auth.PasskeyAuthenticationOptionsModel;
import com.web.gallery.application.model.auth.PasskeyCredentialModel;
import com.web.gallery.application.model.auth.PasskeyRegistrationOptionsModel;
import com.web.gallery.application.model.auth.RefreshTokenModel;
import com.web.gallery.application.model.auth.WebAuthnChallengeModel;
import com.web.gallery.application.repository.account.AccountRepository;
import com.web.gallery.application.repository.auth.PasskeyCredentialRepository;
import com.web.gallery.application.repository.auth.PasskeyUserHandleRepository;
import com.web.gallery.application.repository.auth.RecoveryCodeRepository;
import com.web.gallery.application.repository.auth.RefreshTokenRepository;
import com.web.gallery.application.repository.auth.WebAuthnChallengeRepository;
import com.web.gallery.application.service.auth.AuthService;
import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.constant.MessageConst;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.enumeration.LoginMethodEnum;
import com.web.gallery.domain.enumeration.WebAuthnChallengePurposeEnum;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.exception.InvalidRecoveryCodeException;
import com.web.gallery.domain.exception.InvalidRefreshTokenException;
import com.web.gallery.domain.exception.PasskeyVerificationException;
import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.auth.LastUsedAt;
import com.web.gallery.domain.model.auth.PasskeyChallenge;
import com.web.gallery.domain.model.auth.PasskeyName;
import com.web.gallery.domain.model.auth.PasskeyResponseJson;
import com.web.gallery.domain.model.auth.PasskeyTimeoutMillis;
import com.web.gallery.domain.model.auth.PasskeyUserHandle;
import com.web.gallery.domain.model.auth.RecoveryCode;
import com.web.gallery.domain.model.auth.RefreshTokenValue;
import com.web.gallery.domain.model.auth.RelyingPartyId;
import com.web.gallery.domain.model.auth.RelyingPartyName;
import com.web.gallery.domain.model.common.ExpiresAt;
import com.web.gallery.domain.model.common.IpAddress;
import com.web.gallery.domain.model.common.TokenHash;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JWT認証に関するビジネスロジックを行うServiceの実装クラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtConfig jwtConfig;
  private final LoginConfig loginConfig;
  private final RefreshTokenRepository refreshTokenRepository;
  private final AccountRepository accountRepository;
  private final PasskeyCredentialRepository passkeyCredentialRepository;
  private final PasskeyUserHandleRepository passkeyUserHandleRepository;
  private final WebAuthnChallengeRepository webAuthnChallengeRepository;
  private final RecoveryCodeRepository recoveryCodeRepository;
  private final WebAuthnVerifier webAuthnVerifier;
  private final WebAuthnConfig webAuthnConfig;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final Clock clock;

  public AuthServiceImpl(
      JwtTokenProvider jwtTokenProvider,
      JwtConfig jwtConfig,
      LoginConfig loginConfig,
      RefreshTokenRepository refreshTokenRepository,
      AccountRepository accountRepository,
      PasskeyCredentialRepository passkeyCredentialRepository,
      PasskeyUserHandleRepository passkeyUserHandleRepository,
      WebAuthnChallengeRepository webAuthnChallengeRepository,
      RecoveryCodeRepository recoveryCodeRepository,
      WebAuthnVerifier webAuthnVerifier,
      WebAuthnConfig webAuthnConfig,
      ApplicationEventPublisher applicationEventPublisher,
      Clock clock) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.jwtConfig = jwtConfig;
    this.loginConfig = loginConfig;
    this.refreshTokenRepository = refreshTokenRepository;
    this.accountRepository = accountRepository;
    this.passkeyCredentialRepository = passkeyCredentialRepository;
    this.passkeyUserHandleRepository = passkeyUserHandleRepository;
    this.webAuthnChallengeRepository = webAuthnChallengeRepository;
    this.recoveryCodeRepository = recoveryCodeRepository;
    this.webAuthnVerifier = webAuthnVerifier;
    this.webAuthnConfig = webAuthnConfig;
    this.applicationEventPublisher = applicationEventPublisher;
    this.clock = clock;
  }

  /**
   * パスキー認証の開始時に、クライアントへ渡すオプションを発行する
   *
   * <p>アカウントを特定しない（discoverable credential）ため、未ログインの誰でも呼び出せる。 発行したチャレンジは有効期間（{@code
   * app.webauthn.challenge-timeout-seconds}）内に1度だけ使用できる
   *
   * @return {@link PasskeyAuthenticationOptionsModel}
   */
  @Override
  @Transactional
  public PasskeyAuthenticationOptionsModel createPasskeyAuthenticationOptions() {
    PasskeyChallenge challenge = webAuthnVerifier.generateChallenge();
    webAuthnChallengeRepository.save(
        WebAuthnChallengeModel.of(
            challenge,
            WebAuthnChallengePurposeEnum.AUTHENTICATION,
            null,
            new ExpiresAt(
                OffsetDateTime.now(clock)
                    .plusSeconds(webAuthnConfig.getChallengeTimeoutSeconds()))));

    return PasskeyAuthenticationOptionsModel.builder()
        .challenge(challenge)
        .rpId(new RelyingPartyId(webAuthnConfig.getRpId()))
        .allowCredentialIds(List.of())
        .timeoutMillis(
            new PasskeyTimeoutMillis(webAuthnConfig.getChallengeTimeoutSeconds() * 1000L))
        .build();
  }

  /**
   * パスキーの認証レスポンスを検証し、トークンを発行する
   *
   * <p>管理者ロック中・ログイン失敗回数の上限到達中のアカウントは拒否する （失敗回数によるロックは一定時間経過後に自動解除する）。
   * パスキーの検証失敗はログイン失敗回数に加算しない（署名の総当たりは成立せず、 加算すると第三者が公開されたクレデンシャルIDで本人をロックさせる嫌がらせに使えるため）。
   *
   * @param authenticationResponseJson 認証レスポンス（{@code PublicKeyCredential#toJSON()}の形式）
   * @param ipAddress 送信元IPアドレス（ログイン履歴に記録する）
   * @return {@link AuthTokenModel}
   * @throws PasskeyVerificationException 認証レスポンスの検証に失敗した場合
   * @throws LockedException アカウントがロックされている場合
   * @throws DisabledException アカウントが無効の場合
   */
  @Override
  @Transactional
  public AuthTokenModel loginWithPasskey(
      PasskeyResponseJson authenticationResponseJson, IpAddress ipAddress) {
    PasskeyAssertionModel assertion =
        webAuthnVerifier.readAuthentication(authenticationResponseJson);

    // 同一パスキーによる同時認証を行ロックで直列化し、署名カウンタの検証と更新の間の競合を防ぐ
    PasskeyCredentialModel credential =
        passkeyCredentialRepository.findByCredentialIdForUpdate(assertion.getCredentialId());
    if (credential == null) {
      throw new PasskeyVerificationException("Unknown passkey credential");
    }

    // チャレンジは取得と同時に削除する（1度きり）。未発行・使用済み・期限切れはここで弾く
    if (webAuthnChallengeRepository.consumeForAuthentication(assertion.getChallenge()) == null) {
      throw new PasskeyVerificationException("Passkey authentication challenge is not available");
    }

    AccountModel accountModel = accountRepository.getByAccountNo(credential.getAccountNo());
    if (accountModel == null) {
      throw new PasskeyVerificationException("Passkey owner account not found");
    }

    // 認証器がユーザーハンドルを返した場合は、パスキーの所有者と一致することを確かめる
    PasskeyUserHandle userHandle =
        passkeyUserHandleRepository.findByAccountNo(credential.getAccountNo());
    if (assertion.getUserHandle() != null && !assertion.getUserHandle().equals(userHandle)) {
      throw new PasskeyVerificationException("Passkey user handle mismatch");
    }

    // 同一アカウントへのログイン試行を直列化してからロック状態を判定する
    accountRepository.lockForLoginAttempt(accountModel.getAccountId());
    accountModel = accountRepository.getByAccountNo(credential.getAccountNo());
    if (accountModel == null) {
      throw new PasskeyVerificationException("Passkey owner account not found");
    }
    rejectIfLocked(accountModel);

    AccountPrincipal principal = new AccountPrincipal(accountModel, loginConfig.getFailCount());
    if (!principal.isEnabled()) {
      throw new DisabledException(MessageConst.ERR_BAD_CREDENTIALS);
    }

    PasskeyAssertionResultModel result =
        webAuthnVerifier.verifyAuthentication(
            authenticationResponseJson, assertion.getChallenge(), credential);
    passkeyCredentialRepository.updateAuthenticationResult(
        credential.withAuthenticationResult(result, new LastUsedAt(OffsetDateTime.now(clock))));

    publishLoginSuccess(principal, ipAddress, LoginMethodEnum.PASSKEY);
    return issueTokens(principal);
  }

  /**
   * リカバリーコードで本人確認し、新しいパスキーを作成するためのオプション（チャレンジ等）を発行する
   *
   * <p>照合したリカバリーコードはこの時点で使用済み（削除）にする。発行したチャレンジは当該アカウントに紐づき、 有効期間内に1度だけ{@link
   * #loginWithRecoveryCode}で使用できる。
   *
   * <p>管理者ロック中・ログイン失敗回数の上限到達中のアカウントは、パスキーでのログインと同じく拒否する。
   * コードは80ビットの乱数でオンラインの総当たりは成立しないため、照合の失敗はログイン失敗回数に加算しない
   * （加算すると、公開されているアカウントIDを使って第三者が本人をロックさせる嫌がらせに使えるため）。
   *
   * @param accountId アカウントID
   * @param recoveryCode リカバリーコード
   * @return {@link PasskeyRegistrationOptionsModel}
   * @throws InvalidRecoveryCodeException アカウントが存在しない・無効、またはコードが一致しない（使用済みを含む）場合
   * @throws LockedException アカウントがロックされている場合
   */
  @Override
  @Transactional
  public PasskeyRegistrationOptionsModel createRecoveryOptions(
      AccountId accountId, RecoveryCode recoveryCode) {
    // 同一アカウントへの試行を直列化し、ロック判定とコードの消費の間の競合を防ぐ
    accountRepository.lockForLoginAttempt(accountId);
    AccountModel accountModel = accountRepository.getByAccountId(accountId);
    if (accountModel == null) {
      throw new InvalidRecoveryCodeException("Recovery target account not found");
    }
    rejectIfLocked(accountModel);
    if (!new AccountPrincipal(accountModel, loginConfig.getFailCount()).isEnabled()) {
      throw new InvalidRecoveryCodeException("Recovery target account is disabled");
    }

    AccountNo accountNo = accountModel.getAccountNo();
    if (!recoveryCodeRepository.consume(accountNo, recoveryCode)) {
      throw new InvalidRecoveryCodeException("Recovery code mismatch");
    }

    PasskeyChallenge challenge = webAuthnVerifier.generateChallenge();
    webAuthnChallengeRepository.save(
        WebAuthnChallengeModel.of(
            challenge,
            WebAuthnChallengePurposeEnum.RECOVERY,
            accountNo,
            new ExpiresAt(
                OffsetDateTime.now(clock)
                    .plusSeconds(webAuthnConfig.getChallengeTimeoutSeconds()))));

    return PasskeyRegistrationOptionsModel.builder()
        .challenge(challenge)
        .rpId(new RelyingPartyId(webAuthnConfig.getRpId()))
        .rpName(new RelyingPartyName(webAuthnConfig.getRpName()))
        .userHandle(getOrCreateUserHandle(accountNo))
        .accountId(accountModel.getAccountId())
        .accountName(accountModel.getAccountName())
        .excludeCredentialIds(
            passkeyCredentialRepository.findByAccountNo(accountNo).toCredentialIdList())
        .timeoutMillis(
            new PasskeyTimeoutMillis(webAuthnConfig.getChallengeTimeoutSeconds() * 1000L))
        .build();
  }

  /**
   * 復旧用のパスキーの登録レスポンスを検証してパスキーを登録し、トークンを発行する
   *
   * <p>チャレンジは{@link #createRecoveryOptions}で発行され、未使用かつ有効期限内である必要がある。 復旧を妨げないよう、パスキーの登録数の上限（{@link
   * Consts#PASSKEY_MAX_COUNT}）は適用しない。 ログイン履歴にはリカバリーコードによる復旧として記録する
   *
   * @param registrationResponseJson 登録レスポンス（{@code PublicKeyCredential#toJSON()}の形式）
   * @param passkeyName パスキー名
   * @param ipAddress 送信元IPアドレス（ログイン履歴に記録する）
   * @return {@link AuthTokenModel}
   * @throws PasskeyVerificationException 登録レスポンスの検証に失敗した場合
   * @throws LockedException アカウントがロックされている場合
   * @throws DisabledException アカウントが無効の場合
   * @throws GalleryException 登録済みのパスキーの場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public AuthTokenModel loginWithRecoveryCode(
      PasskeyResponseJson registrationResponseJson, PasskeyName passkeyName, IpAddress ipAddress)
      throws GalleryException {
    // 署名・origin・RP ID・UV等の検証（DBを更新しない）を先に行い、通過したものだけチャレンジを消費する
    PasskeyChallenge challenge =
        webAuthnVerifier.readRegistrationChallenge(registrationResponseJson);
    PasskeyAttestationModel attestation =
        webAuthnVerifier.verifyRegistration(registrationResponseJson, challenge);
    WebAuthnChallengeModel storedChallenge =
        webAuthnChallengeRepository.consumeForRecovery(challenge);
    if (storedChallenge == null || storedChallenge.getAccountNo() == null) {
      throw new PasskeyVerificationException("Passkey recovery challenge is not available");
    }

    AccountModel accountModel = accountRepository.getByAccountNo(storedChallenge.getAccountNo());
    if (accountModel == null) {
      throw new PasskeyVerificationException("Recovery target account not found");
    }
    accountRepository.lockForLoginAttempt(accountModel.getAccountId());
    accountModel = accountRepository.getByAccountNo(storedChallenge.getAccountNo());
    if (accountModel == null) {
      throw new PasskeyVerificationException("Recovery target account not found");
    }
    rejectIfLocked(accountModel);

    AccountPrincipal principal = new AccountPrincipal(accountModel, loginConfig.getFailCount());
    if (!principal.isEnabled()) {
      throw new DisabledException(MessageConst.ERR_BAD_CREDENTIALS);
    }

    if (passkeyCredentialRepository.findByCredentialId(attestation.getCredentialId()) != null) {
      throw ErrorEnum.DUPLICATE_PASSKEY.toException();
    }
    passkeyCredentialRepository.save(
        PasskeyCredentialModel.forRegist(accountModel.getAccountNo(), attestation, passkeyName));

    publishLoginSuccess(principal, ipAddress, LoginMethodEnum.RECOVERY_CODE);
    return issueTokens(principal);
  }

  /**
   * リフレッシュトークンを検証し、新しいアクセストークンを発行する
   *
   * <p>リフレッシュのたびに新しいリフレッシュトークンを発行して旧トークンを無効化する（トークンローテーション）。
   * 無効化済みトークンの再利用を検知した場合は盗用の疑いがあるため、該当アカウントの全トークンを失効させる
   *
   * @param refreshToken リフレッシュトークン
   * @return {@link AuthTokenModel}
   * @throws InvalidRefreshTokenException トークンが無効な場合
   * @throws LockedException アカウントがロックされている場合
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
        log.info(
            "Revoked refresh token reused within grace period. Treated as a benign retry. (accountNo: {})",
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
    refreshTokenRepository.save(
        RefreshTokenModel.of(
            storedToken.getAccountNo(),
            new TokenHash(hashToken(newRefreshToken)),
            new ExpiresAt(
                OffsetDateTime.now(clock).plusDays(jwtConfig.getRefreshTokenExpirationDays()))));

    String accessToken = jwtTokenProvider.generateAccessToken(principal);

    return AuthTokenModel.of(
        accessToken, newRefreshToken, (long) jwtConfig.getAccessTokenExpirationMinutes() * 60);
  }

  /**
   * ログアウトし、リフレッシュトークンを無効化する
   *
   * @param refreshToken リフレッシュトークン
   */
  @Override
  @Transactional
  public void logout(RefreshTokenValue refreshToken) {
    refreshTokenRepository.revokeByTokenHash(new TokenHash(hashToken(refreshToken.value())));
  }

  /** 有効期限切れのリフレッシュトークンをDBから削除する */
  @Override
  @Transactional
  public void purgeExpiredRefreshTokens() {
    refreshTokenRepository.deleteExpired();
  }

  /** 有効期限切れのWebAuthnチャレンジをDBから削除する */
  @Override
  @Transactional
  public void purgeExpiredWebAuthnChallenges() {
    webAuthnChallengeRepository.deleteExpired();
  }

  /**
   * 認証成功イベントを発行する
   *
   * <p>ログイン失敗回数のリセット・最終ログイン日時の更新・ログイン履歴の記録（AccountServiceImpl#handle）を、 ログイン方式によらず共通化する
   *
   * @param principal 認証済みの{@link AccountPrincipal}
   * @param ipAddress 送信元IPアドレス
   * @param loginMethod ログイン方式
   */
  private void publishLoginSuccess(
      AccountPrincipal principal, IpAddress ipAddress, LoginMethodEnum loginMethod) {
    UsernamePasswordAuthenticationToken authentication =
        UsernamePasswordAuthenticationToken.authenticated(
            principal, null, principal.getAuthorities());
    authentication.setDetails(LoginContextModel.of(ipAddress, loginMethod));
    applicationEventPublisher.publishEvent(new AuthenticationSuccessEvent(authentication));
  }

  /**
   * アカウントのパスキー用ユーザーハンドルを取得する。未発行であれば発行する
   *
   * @param accountNo アカウント番号
   * @return {@link PasskeyUserHandle}
   */
  private PasskeyUserHandle getOrCreateUserHandle(AccountNo accountNo) {
    PasskeyUserHandle userHandle = passkeyUserHandleRepository.findByAccountNo(accountNo);
    if (userHandle != null) {
      return userHandle;
    }
    passkeyUserHandleRepository.saveIfAbsent(accountNo, webAuthnVerifier.generateUserHandle());
    return passkeyUserHandleRepository.findByAccountNo(accountNo);
  }

  /**
   * ログインに成功したアカウントへ、アクセストークンとリフレッシュトークンを発行する
   *
   * <p>既存のリフレッシュトークンはすべて無効化する（同時セッション制限）
   *
   * @param principal 認証済みの{@link AccountPrincipal}
   * @return {@link AuthTokenModel}
   */
  private AuthTokenModel issueTokens(AccountPrincipal principal) {
    AccountNo accountNo = new AccountNo(principal.getAccountNo());
    refreshTokenRepository.revokeAllByAccountNo(accountNo);

    String accessToken = jwtTokenProvider.generateAccessToken(principal);
    String refreshToken = jwtTokenProvider.generateRefreshToken();

    // リフレッシュトークンをDB保存（ハッシュ化して保存）
    refreshTokenRepository.save(
        RefreshTokenModel.of(
            accountNo,
            new TokenHash(hashToken(refreshToken)),
            new ExpiresAt(
                OffsetDateTime.now(clock).plusDays(jwtConfig.getRefreshTokenExpirationDays()))));

    return AuthTokenModel.of(
        accessToken, refreshToken, (long) jwtConfig.getAccessTokenExpirationMinutes() * 60);
  }

  /**
   * アカウントがロック中であればログインを拒否する
   *
   * <p>ログイン失敗回数によるロックは、最終失敗から一定時間が経過していれば自動解除する （総当たり攻撃中は失敗のたびに更新時刻が進むため解除されない）。管理者ロックは自動解除しない
   *
   * @param accountModel {@link AccountModel}（null可）
   * @throws LockedException ロック中の場合
   */
  private void rejectIfLocked(AccountModel accountModel) {
    if (!isLocked(accountModel)) {
      return;
    }
    if (isLockDurationElapsed(accountModel)) {
      releaseLock(accountModel);
      return;
    }
    throw new LockedException(MessageConst.ERR_ACCOUNT_LOCKED);
  }

  /**
   * アカウントがロック状態（管理者ロック、またはログイン失敗回数の上限到達）かどうかを判定する
   *
   * @param accountModel {@link AccountModel}（null可）
   * @return ロック状態の場合true
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
   * @param accountModel {@link AccountModel}（null可）
   * @return 管理者ロックされている場合true
   */
  private boolean isAdminLocked(AccountModel accountModel) {
    return accountModel != null
        && accountModel.getIsAdminLocked() != null
        && Boolean.TRUE.equals(accountModel.getIsAdminLocked().value());
  }

  /**
   * 無効化済みリフレッシュトークンの再送が、ローテーション直後の猶予期間内かどうかを判定する
   *
   * <p>{@code updated_at}（＝ローテーションで無効化した時刻）から {@code app.jwt.refreshTokenReuseGraceSeconds}
   * 秒以内であれば正常系のリトライとみなす。 更新日時が取得できない場合は猶予対象外（盗用対応を優先）とする。
   *
   * @param storedToken DBから取得した{@link RefreshTokenModel}
   * @return 猶予期間内の場合true
   */
  private boolean isWithinReuseGracePeriod(RefreshTokenModel storedToken) {
    if (storedToken.getUpdatedAt() == null) {
      return false;
    }
    return storedToken
        .getUpdatedAt()
        .value()
        .plusSeconds(jwtConfig.getRefreshTokenReuseGraceSeconds())
        .isAfter(OffsetDateTime.now(clock));
  }

  /**
   * アカウントの最終更新（＝直近のログイン失敗）から、ロック自動解除までの時間が経過しているかどうかを判定する
   *
   * <p>管理者による強制ロックは自動解除の対象外（管理者による解除のみで解ける）
   *
   * @param accountModel {@link AccountModel}（null可）
   * @return 自動解除可能な場合true
   */
  private boolean isLockDurationElapsed(AccountModel accountModel) {
    if (accountModel == null
        || accountModel.getUpdatedAt() == null
        || isAdminLocked(accountModel)) {
      return false;
    }
    return accountModel
        .getUpdatedAt()
        .value()
        .plusMinutes(loginConfig.getLockDurationMinutes())
        .isBefore(OffsetDateTime.now(clock));
  }

  /**
   * アカウントロックを解除する（ログイン失敗回数を0にリセットする）
   *
   * <p>解除対象の行は直前に取得済みで必ず存在するため通常は失敗しないが、 万一失敗しても認証処理は継続する（次回試行で再度解除を試みる）
   *
   * @param accountModel {@link AccountModel}
   */
  private void releaseLock(AccountModel accountModel) {
    try {
      accountRepository.updateLoginFailureCount(
          AccountModel.forUnlock(accountModel.getAccountNo().value()));
    } catch (GalleryException e) {
      log.warn(
          "Failed to auto-release account lock. (accountNo: {})",
          accountModel.getAccountNo().value(),
          e);
    }
  }

  /**
   * トークンをSHA-256でハッシュ化する
   *
   * @param token ハッシュ化対象のトークン文字列
   * @return ハッシュ化された文字列（16進数表記）
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
