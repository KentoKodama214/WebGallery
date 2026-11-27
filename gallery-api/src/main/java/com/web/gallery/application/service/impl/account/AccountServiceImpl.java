package com.web.gallery.application.service.impl.account;

import com.web.gallery.application.AccountPrincipal;
import com.web.gallery.application.aggregate.Account;
import com.web.gallery.application.config.AccountConfig;
import com.web.gallery.application.config.LoginConfig;
import com.web.gallery.application.config.WebAuthnConfig;
import com.web.gallery.application.helper.GeoIpResolver;
import com.web.gallery.application.helper.ReauthenticationThrottle;
import com.web.gallery.application.helper.WebAuthnVerifier;
import com.web.gallery.application.model.account.AccountGetModel;
import com.web.gallery.application.model.account.AccountListGetModel;
import com.web.gallery.application.model.account.AccountModel;
import com.web.gallery.application.model.account.AccountPageModel;
import com.web.gallery.application.model.account.LoginHistoryModel;
import com.web.gallery.application.model.auth.LoginContextModel;
import com.web.gallery.application.model.auth.PasskeyAssertionModel;
import com.web.gallery.application.model.auth.PasskeyAssertionResultModel;
import com.web.gallery.application.model.auth.PasskeyAttestationModel;
import com.web.gallery.application.model.auth.PasskeyAuthenticationOptionsModel;
import com.web.gallery.application.model.auth.PasskeyCredentialModel;
import com.web.gallery.application.model.auth.PasskeyCredentialModelList;
import com.web.gallery.application.model.auth.PasskeyRegistrationOptionsModel;
import com.web.gallery.application.model.auth.RecoveryCodeList;
import com.web.gallery.application.model.auth.WebAuthnChallengeModel;
import com.web.gallery.application.model.common.KbnMstModelList;
import com.web.gallery.application.repository.account.AccountAggregateRepository;
import com.web.gallery.application.repository.account.AccountRepository;
import com.web.gallery.application.repository.account.LoginHistoryRepository;
import com.web.gallery.application.repository.auth.PasskeyCredentialRepository;
import com.web.gallery.application.repository.auth.PasskeyUserHandleRepository;
import com.web.gallery.application.repository.auth.RecoveryCodeRepository;
import com.web.gallery.application.repository.auth.RefreshTokenRepository;
import com.web.gallery.application.repository.auth.WebAuthnChallengeRepository;
import com.web.gallery.application.repository.common.KbnMstRepository;
import com.web.gallery.application.repository.inquiry.InquiryReplyMstRepository;
import com.web.gallery.application.repository.photo.FileRepository;
import com.web.gallery.application.service.account.AccountService;
import com.web.gallery.domain.constant.Consts;
import com.web.gallery.domain.constant.MessageConst;
import com.web.gallery.domain.enumeration.ErrorEnum;
import com.web.gallery.domain.enumeration.WebAuthnChallengePurposeEnum;
import com.web.gallery.domain.event.AccountAuthorityChangedEvent;
import com.web.gallery.domain.event.AccountDeletedEvent;
import com.web.gallery.domain.event.AccountLockedEvent;
import com.web.gallery.domain.event.AccountRegisteredEvent;
import com.web.gallery.domain.event.AccountUnlockedEvent;
import com.web.gallery.domain.event.AccountUpdatedEvent;
import com.web.gallery.domain.event.PhotoDeletedEvent;
import com.web.gallery.domain.exception.GalleryException;
import com.web.gallery.domain.exception.PasskeyVerificationException;
import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountName;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.auth.LastUsedAt;
import com.web.gallery.domain.model.auth.PasskeyChallenge;
import com.web.gallery.domain.model.auth.PasskeyName;
import com.web.gallery.domain.model.auth.PasskeyNo;
import com.web.gallery.domain.model.auth.PasskeyResponseJson;
import com.web.gallery.domain.model.auth.PasskeyTimeoutMillis;
import com.web.gallery.domain.model.auth.PasskeyUserHandle;
import com.web.gallery.domain.model.auth.RecoveryCode;
import com.web.gallery.domain.model.auth.RelyingPartyId;
import com.web.gallery.domain.model.auth.RelyingPartyName;
import com.web.gallery.domain.model.common.ExpiresAt;
import com.web.gallery.domain.model.common.IpGeoLocation;
import com.web.gallery.domain.model.common.KbnClassCode;
import com.web.gallery.domain.model.photo.ImageFilePath;
import com.web.gallery.domain.model.photo.PhotoNo;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** アカウントに関するビジネスロジックを行うServiceの実装クラス */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements UserDetailsService, AccountService {

  /** リカバリーコードの生成に使う暗号論的乱数生成器（スレッドセーフ） */
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final AccountRepository accountRepository;
  private final AccountAggregateRepository accountAggregateRepository;
  private final FileRepository fileRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final LoginHistoryRepository loginHistoryRepository;
  private final KbnMstRepository kbnMstRepository;
  private final InquiryReplyMstRepository inquiryReplyMstRepository;
  private final ReauthenticationThrottle reauthenticationThrottle;
  private final PasskeyCredentialRepository passkeyCredentialRepository;
  private final PasskeyUserHandleRepository passkeyUserHandleRepository;
  private final WebAuthnChallengeRepository webAuthnChallengeRepository;
  private final RecoveryCodeRepository recoveryCodeRepository;
  private final WebAuthnVerifier webAuthnVerifier;
  private final WebAuthnConfig webAuthnConfig;
  private final GeoIpResolver geoIpResolver;
  private final LoginConfig loginConfig;
  private final AccountConfig accountConfig;
  private final Clock clock;
  private final ApplicationEventPublisher applicationEventPublisher;

  /**
   * アカウントIDからアカウント情報の存在を確認する
   *
   * @param username アカウントID
   * @return {@link UserDetails}
   * @throws UsernameNotFoundException ユーザーが存在しない場合
   */
  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    AccountModel accountModel = accountRepository.getByAccountId(new AccountId(username));

    if (Objects.isNull(accountModel)) {
      log.info("User not found. (username: {})", username);
      throw new UsernameNotFoundException(MessageConst.USER_NOT_FOUND);
    }
    return new AccountPrincipal(accountModel, loginConfig.getFailCount());
  }

  /**
   * アカウント新規登録の開始時に、最初のパスキーを作成するためのオプション（チャレンジ等）を発行する
   *
   * <p>アカウントはまだ作成しない。払い出したユーザーハンドル（WebAuthnのuser.id）をチャレンジとともに保存し、 {@link
   * #registAccount}で同じチャレンジが使われたときに、そのユーザーハンドルでアカウントへ紐づける （ログイン時に認証器が返すユーザーハンドルと一致させるため）
   *
   * @param accountId 登録するアカウントID（WebAuthnのuser.name）
   * @param accountName 登録するアカウント名（WebAuthnのuser.displayName）
   * @return {@link PasskeyRegistrationOptionsModel}
   * @throws GalleryException アカウントIDが既に使われている場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public PasskeyRegistrationOptionsModel createSignupOptions(
      AccountId accountId, AccountName accountName) throws GalleryException {
    // パスキーを作らせてから重複に気づかせることがないよう、先に判定する（確定的な判定は登録時に行う）
    if (accountRepository.isExistAccount(accountId)) {
      throw ErrorEnum.DUPLICATE_ACCOUNT_ID.toException();
    }

    PasskeyChallenge challenge = webAuthnVerifier.generateChallenge();
    PasskeyUserHandle userHandle = webAuthnVerifier.generateUserHandle();
    webAuthnChallengeRepository.save(
        WebAuthnChallengeModel.forSignup(challenge, userHandle, challengeExpiresAt()));

    return PasskeyRegistrationOptionsModel.builder()
        .challenge(challenge)
        .rpId(new RelyingPartyId(webAuthnConfig.getRpId()))
        .rpName(new RelyingPartyName(webAuthnConfig.getRpName()))
        .userHandle(userHandle)
        .accountId(accountId)
        .accountName(accountName)
        .excludeCredentialIds(List.of())
        .timeoutMillis(challengeTimeoutMillis())
        .build();
  }

  /**
   * パスキーの登録レスポンスを検証し、アカウントと最初のパスキーを新規登録する
   *
   * <p>チャレンジは{@link #createSignupOptions}で発行され、未使用かつ有効期限内である必要がある。
   * あわせてリカバリーコードを発行し、平文は戻り値でのみ返す（DBにはハッシュのみ保存する）
   *
   * @param accountModel {@link AccountModel}
   * @param registrationResponseJson 登録レスポンス（{@code PublicKeyCredential#toJSON()}の形式）
   * @param passkeyName パスキー名
   * @return 発行したリカバリーコード
   * @throws GalleryException 登録レスポンスの検証に失敗した場合、アカウントIDが既に使われている場合、または登録済みのパスキーの場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public RecoveryCodeList registAccount(
      AccountModel accountModel,
      PasskeyResponseJson registrationResponseJson,
      PasskeyName passkeyName)
      throws GalleryException {
    validatePrefectureCodes(accountModel);

    PasskeyAttestationModel attestation;
    PasskeyUserHandle userHandle;
    try {
      // 署名・origin・RP ID・UV等の検証（DBを更新しない）を先に行い、通過したものだけチャレンジを消費する
      PasskeyChallenge challenge =
          webAuthnVerifier.readRegistrationChallenge(registrationResponseJson);
      attestation = webAuthnVerifier.verifyRegistration(registrationResponseJson, challenge);
      WebAuthnChallengeModel storedChallenge =
          webAuthnChallengeRepository.consumeForSignup(challenge);
      if (storedChallenge == null || storedChallenge.getUserHandle() == null) {
        throw new PasskeyVerificationException("Passkey signup challenge is not available");
      }
      userHandle = storedChallenge.getUserHandle();
    } catch (PasskeyVerificationException e) {
      log.info(
          "Passkey signup verification failed. (accountId: {}, reason: {})",
          accountModel.getAccountId().value(),
          e.getMessage());
      throw ErrorEnum.INVALID_PASSKEY_REGISTRATION.toException();
    }

    if (accountRepository.isExistAccount(accountModel.getAccountId())) {
      throw ErrorEnum.DUPLICATE_ACCOUNT_ID.toException();
    }
    if (passkeyCredentialRepository.findByCredentialId(attestation.getCredentialId()) != null) {
      throw ErrorEnum.DUPLICATE_PASSKEY.toException();
    }

    accountRepository.regist(accountModel);
    AccountNo accountNo =
        accountRepository.getByAccountId(accountModel.getAccountId()).getAccountNo();
    passkeyUserHandleRepository.saveIfAbsent(accountNo, userHandle);
    passkeyCredentialRepository.save(
        PasskeyCredentialModel.forRegist(accountNo, attestation, passkeyName));
    RecoveryCodeList recoveryCodes = issueRecoveryCodes(accountNo);

    applicationEventPublisher.publishEvent(new AccountRegisteredEvent(accountModel.getAccountId()));
    return recoveryCodes;
  }

  /**
   * アカウントを更新する
   *
   * <p>アカウントIDを変更する場合は、当該アカウントのリフレッシュトークンをすべて失効させ、全セッションでの再ログインを強制する
   * （アカウントID変更後にアクセストークンのsubjectが宙に浮くのを防ぐため）
   *
   * @param accountModel {@link AccountModel}
   * @return アカウントIDが重複しており更新をスキップした場合、true
   * @throws GalleryException 更新に失敗した場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public Boolean updateAccount(AccountModel accountModel) throws GalleryException {
    validatePrefectureCodes(accountModel);

    // 変更後のアカウントIDが他アカウントと重複する場合は更新をスキップする
    Boolean isExist =
        accountRepository.isExistAccount(accountModel.getAccountNo(), accountModel.getAccountId());
    if (isExist) {
      return true;
    }

    AccountModel currentAccount = accountRepository.getByAccountNo(accountModel.getAccountNo());

    // アカウントIDが変わると既存のアクセストークンのsubjectが解決不能になるため検知する
    boolean isAccountIdChanged =
        currentAccount != null
            && currentAccount.getAccountId() != null
            && !currentAccount.getAccountId().value().equals(accountModel.getAccountId().value());

    accountRepository.update(accountModel);
    if (isAccountIdChanged) {
      refreshTokenRepository.revokeAllByAccountNo(accountModel.getAccountNo());
    }
    // 通常、認証済みユーザーのアカウントは必ず取得できるため previousAccountId は非null。
    // DB不整合等で更新前のアカウントIDが取得できない場合のみ null とし、
    // リスナー側でキャッシュ全消去にフォールバックさせる（旧IDのエントリを取り残さないため）。
    AccountId previousAccountId =
        currentAccount != null && currentAccount.getAccountId() != null
            ? currentAccount.getAccountId()
            : null;
    applicationEventPublisher.publishEvent(
        new AccountUpdatedEvent(
            accountModel.getAccountNo(), accountModel.getAccountId(), previousAccountId));
    return false;
  }

  /**
   * アカウントIDからアカウント情報を取得する
   *
   * @param accountId アカウントID
   * @return {@link AccountModel}
   */
  @Override
  @Transactional(readOnly = true)
  public AccountModel getAccountById(AccountId accountId) {
    return accountRepository.getByAccountId(accountId);
  }

  /**
   * アカウントの一覧を、ページング情報に従い取得する
   *
   * @param accountListGetModel {@link AccountListGetModel}
   * @return {@link AccountPageModel}
   */
  @Override
  @Transactional(readOnly = true)
  public AccountPageModel getAccountList(AccountListGetModel accountListGetModel) {
    AccountGetModel accountGetModel =
        AccountGetModel.of(accountListGetModel, accountConfig.getAccountCountPerPage());
    AccountPageModel accountPageModel = accountRepository.getAccountList(accountGetModel);
    return AccountPageModel.of(
        accountPageModel.getAccountModelList().sortByAccountId(), accountPageModel.getIsLast());
  }

  /**
   * 管理者用：削除済みを含む全アカウントの一覧を、ページング情報に従い取得する
   *
   * @param accountListGetModel {@link AccountListGetModel}
   * @return {@link AccountPageModel}
   */
  @Override
  @Transactional(readOnly = true)
  public AccountPageModel getAccountListForAdmin(AccountListGetModel accountListGetModel) {
    AccountGetModel accountGetModel =
        AccountGetModel.of(accountListGetModel, accountConfig.getAccountCountPerPage());
    AccountPageModel accountPageModel = accountRepository.getAccountListForAdmin(accountGetModel);
    return AccountPageModel.of(
        accountPageModel.getAccountModelList().sortByAccountId(), accountPageModel.getIsLast());
  }

  /**
   * 管理者用：アカウントのロックを解除する（管理者ロックフラグを解除し、ログイン失敗回数も0にリセット）
   *
   * @param accountNo アカウント番号
   * @throws GalleryException 更新に失敗した場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public void unlockAccount(AccountNo accountNo) throws GalleryException {
    // 管理者ロックとログイン失敗回数の両方を解除する
    accountRepository.updateLoginFailureCount(AccountModel.forAdminUnlock(accountNo.value()));
    applicationEventPublisher.publishEvent(new AccountUnlockedEvent(accountNo));
  }

  /**
   * 管理者用：アカウントを強制ロックする
   *
   * <p>管理者ロックフラグを立てる（ログイン失敗回数による自動解除の対象外）。 あわせてログイン失敗回数も上限値に設定する（管理画面の表示・判定と整合させるため）
   *
   * @param accountNo アカウント番号
   * @throws GalleryException 更新に失敗した場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public void lockAccount(AccountNo accountNo) throws GalleryException {
    accountRepository.updateLoginFailureCount(
        AccountModel.forLock(accountNo.value(), loginConfig.getFailCount()));
    applicationEventPublisher.publishEvent(new AccountLockedEvent(accountNo));
  }

  /**
   * 管理者用：アカウントの権限を変更する
   *
   * @param accountModel {@link AccountModel}（{@link AccountModel#forAuthorityChange}で生成したもの）
   * @throws GalleryException 更新に失敗した場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public void updateAccountAuthority(AccountModel accountModel) throws GalleryException {
    accountRepository.updateAuthority(accountModel);
    applicationEventPublisher.publishEvent(
        new AccountAuthorityChangedEvent(accountModel.getAccountNo()));
  }

  /**
   * アカウントを削除する
   *
   * <p>パスキーによる本人確認（再認証）を行ったうえで物理削除する
   *
   * <p>管理者として他ユーザーのお問い合わせへ投稿した返信が残っている場合は削除しない。返信を巻き込んで
   * 削除すると、無関係な第三者のお問い合わせスレッドから回答本文だけが消え、ステータス（回答済み）と 実データが食い違うため、業務ルールとして禁止する。
   * 自分が起票したお問い合わせへの自己返信はスレッドごと削除されるため、この判定には含めない
   *
   * <p>ブロックされたアカウントを退会させるには、返信を別の管理者へ引き継ぐ運用作業が必要になる （手順は{@code
   * doc/architecture/security.md}「管理者返信を持つアカウントの退会」を参照）
   *
   * @param accountNo アカウント番号
   * @param accountId アカウントID
   * @param reauthenticationResponseJson 本人確認用の認証レスポンス
   * @throws GalleryException 本人確認に失敗した場合、または管理者として投稿した返信が残っている場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public void deleteAccount(
      AccountNo accountNo, AccountId accountId, PasskeyResponseJson reauthenticationResponseJson)
      throws GalleryException {
    verifyReauthentication(
        accountNo, accountRepository.getByAccountNo(accountNo), reauthenticationResponseJson);

    if (inquiryReplyMstRepository.existsReplyToOthersInquiry(accountNo)) {
      throw ErrorEnum.CANNOT_DELETE_ACCOUNT_WITH_ADMIN_REPLY.toException();
    }

    Account account = Account.forDelete(accountNo);
    accountAggregateRepository.delete(account);

    // 写真ファイルのディレクトリ削除はDBコミット確定後に行う（ロールバック時の不整合を防ぐ）
    // S3上は「{アカウント番号}/」プレフィックス配下を一括削除する。プレフィックスには変更されうる
    // アカウントIDではなく不変のアカウント番号を用いるため、ID変更後の退会でも取り残しが発生しない
    deletePhotoDirectoryAfterCommit(new ImageFilePath(accountNo.value() + "/"));

    for (PhotoNo photoNo : account.getDeletedPhotoNoList()) {
      applicationEventPublisher.publishEvent(new PhotoDeletedEvent(accountNo, photoNo));
    }
    applicationEventPublisher.publishEvent(new AccountDeletedEvent(accountNo, accountId));
  }

  /**
   * 重要操作の前の本人確認に使う、認証オプション（チャレンジ等）を発行する
   *
   * <p>発行したチャレンジは呼び出したアカウントに紐づき、有効期間内に1度だけ本人確認に使用できる。
   * allowCredentialsを本人のパスキーに限定し、別アカウントのパスキーを選ばせない
   *
   * @param accountNo アカウント番号（セッション由来）
   * @return {@link PasskeyAuthenticationOptionsModel}
   */
  @Override
  @Transactional
  public PasskeyAuthenticationOptionsModel createReauthenticationOptions(AccountNo accountNo) {
    PasskeyChallenge challenge = webAuthnVerifier.generateChallenge();
    webAuthnChallengeRepository.save(
        WebAuthnChallengeModel.of(
            challenge,
            WebAuthnChallengePurposeEnum.REAUTHENTICATION,
            accountNo,
            challengeExpiresAt()));

    return PasskeyAuthenticationOptionsModel.builder()
        .challenge(challenge)
        .rpId(new RelyingPartyId(webAuthnConfig.getRpId()))
        .allowCredentialIds(
            passkeyCredentialRepository.findByAccountNo(accountNo).toCredentialIdList())
        .timeoutMillis(challengeTimeoutMillis())
        .build();
  }

  /**
   * パスキー登録の開始時に、クライアントへ渡すオプション（チャレンジ等）を発行する
   *
   * <p>アクセストークンを盗んだ攻撃者が自分のパスキーを登録して居座るのを防ぐため、パスキーによる本人確認（再認証）を必須とする。
   * 発行したチャレンジは呼び出したアカウントに紐づき、有効期間内に1度だけ登録に使用できる
   *
   * @param accountNo アカウント番号（セッション由来）
   * @param reauthenticationResponseJson 本人確認用の認証レスポンス
   * @return {@link PasskeyRegistrationOptionsModel}
   * @throws GalleryException 本人確認に失敗した場合、またはパスキーの登録数が上限に達している場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public PasskeyRegistrationOptionsModel createPasskeyRegistrationOptions(
      AccountNo accountNo, PasskeyResponseJson reauthenticationResponseJson)
      throws GalleryException {
    AccountModel accountModel = accountRepository.getByAccountNo(accountNo);
    verifyReauthentication(accountNo, accountModel, reauthenticationResponseJson);

    if (passkeyCredentialRepository.countByAccountNo(accountNo) >= Consts.PASSKEY_MAX_COUNT) {
      throw ErrorEnum.REACHED_PASSKEY_LIMIT.toException();
    }

    PasskeyChallenge challenge = webAuthnVerifier.generateChallenge();
    webAuthnChallengeRepository.save(
        WebAuthnChallengeModel.of(
            challenge, WebAuthnChallengePurposeEnum.REGISTRATION, accountNo, challengeExpiresAt()));

    return PasskeyRegistrationOptionsModel.builder()
        .challenge(challenge)
        .rpId(new RelyingPartyId(webAuthnConfig.getRpId()))
        .rpName(new RelyingPartyName(webAuthnConfig.getRpName()))
        .userHandle(getOrCreateUserHandle(accountNo))
        .accountId(accountModel.getAccountId())
        .accountName(accountModel.getAccountName())
        .excludeCredentialIds(
            passkeyCredentialRepository.findByAccountNo(accountNo).toCredentialIdList())
        .timeoutMillis(challengeTimeoutMillis())
        .build();
  }

  /**
   * パスキーの登録レスポンスを検証し、パスキーを登録する
   *
   * <p>チャレンジは{@link #createPasskeyRegistrationOptions}で同じアカウントに発行され、未使用かつ有効期限内である必要がある
   * （本人確認を経ずに発行されたチャレンジや他人のチャレンジでは登録できない）。
   *
   * @param accountNo アカウント番号（セッション由来）
   * @param registrationResponseJson 登録レスポンス（{@code PublicKeyCredential#toJSON()}の形式）
   * @param passkeyName パスキー名
   * @return 登録した{@link PasskeyCredentialModel}
   * @throws GalleryException 登録レスポンスの検証に失敗した場合、登録済みのパスキーの場合、または登録数が上限に達している場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public PasskeyCredentialModel registerPasskey(
      AccountNo accountNo, PasskeyResponseJson registrationResponseJson, PasskeyName passkeyName)
      throws GalleryException {
    PasskeyAttestationModel attestation;
    try {
      // 署名・origin・RP ID・UV等の検証（DBを更新しない）を先に行い、通過したものだけチャレンジを消費する
      PasskeyChallenge challenge =
          webAuthnVerifier.readRegistrationChallenge(registrationResponseJson);
      attestation = webAuthnVerifier.verifyRegistration(registrationResponseJson, challenge);
      WebAuthnChallengeModel storedChallenge =
          webAuthnChallengeRepository.consumeForRegistration(challenge);
      if (storedChallenge == null || !accountNo.equals(storedChallenge.getAccountNo())) {
        throw new PasskeyVerificationException(
            "Passkey registration challenge is not available for this account");
      }
    } catch (PasskeyVerificationException e) {
      log.info(
          "Passkey registration verification failed. (accountNo: {}, reason: {})",
          accountNo.value(),
          e.getMessage());
      throw ErrorEnum.INVALID_PASSKEY_REGISTRATION.toException();
    }

    // 同一アカウントの登録を直列化し、登録数の上限チェックをすり抜けないようにする
    accountRepository.lockForUpdate(accountNo);
    if (passkeyCredentialRepository.countByAccountNo(accountNo) >= Consts.PASSKEY_MAX_COUNT) {
      throw ErrorEnum.REACHED_PASSKEY_LIMIT.toException();
    }
    if (passkeyCredentialRepository.findByCredentialId(attestation.getCredentialId()) != null) {
      throw ErrorEnum.DUPLICATE_PASSKEY.toException();
    }

    passkeyCredentialRepository.save(
        PasskeyCredentialModel.forRegist(accountNo, attestation, passkeyName));
    return passkeyCredentialRepository.findByCredentialId(attestation.getCredentialId());
  }

  /**
   * アカウントに登録されているパスキーを登録日時の昇順で取得する
   *
   * @param accountNo アカウント番号（セッション由来）
   * @return {@link PasskeyCredentialModelList}
   */
  @Override
  @Transactional(readOnly = true)
  public PasskeyCredentialModelList getPasskeyList(AccountNo accountNo) {
    return passkeyCredentialRepository.findByAccountNo(accountNo);
  }

  /**
   * パスキー名を変更する
   *
   * @param accountNo アカウント番号（セッション由来）
   * @param passkeyNo パスキー番号
   * @param passkeyName 変更後のパスキー名
   * @throws GalleryException 自身のパスキーとして存在しない場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public void renamePasskey(AccountNo accountNo, PasskeyNo passkeyNo, PasskeyName passkeyName)
      throws GalleryException {
    if (passkeyCredentialRepository.rename(accountNo, passkeyNo, passkeyName) == 0) {
      throw ErrorEnum.PASSKEY_NOT_FOUND.toException();
    }
  }

  /**
   * パスキーを削除する
   *
   * <p>ログイン手段を失わせる操作のため、パスキーによる本人確認（再認証）を必須とする。 パスキーがログインの唯一の手段のため、最後の1件は削除させない
   *
   * @param accountNo アカウント番号（セッション由来）
   * @param passkeyNo パスキー番号
   * @param reauthenticationResponseJson 本人確認用の認証レスポンス
   * @throws GalleryException 本人確認に失敗した場合、自身のパスキーとして存在しない場合、または最後の1件の場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public void deletePasskey(
      AccountNo accountNo, PasskeyNo passkeyNo, PasskeyResponseJson reauthenticationResponseJson)
      throws GalleryException {
    verifyReauthentication(
        accountNo, accountRepository.getByAccountNo(accountNo), reauthenticationResponseJson);

    // 同一アカウントの削除を直列化し、並行した削除で最後の1件まで消えないようにする
    accountRepository.lockForUpdate(accountNo);
    PasskeyCredentialModelList passkeys = passkeyCredentialRepository.findByAccountNo(accountNo);
    if (passkeys.stream().noneMatch(passkey -> passkeyNo.equals(passkey.getPasskeyNo()))) {
      throw ErrorEnum.PASSKEY_NOT_FOUND.toException();
    }
    if (passkeys.size() <= 1) {
      throw ErrorEnum.CANNOT_DELETE_LAST_PASSKEY.toException();
    }
    if (passkeyCredentialRepository.delete(accountNo, passkeyNo) == 0) {
      throw ErrorEnum.PASSKEY_NOT_FOUND.toException();
    }
  }

  /**
   * 未使用のリカバリーコードの件数を取得する
   *
   * @param accountNo アカウント番号（セッション由来）
   * @return 件数
   */
  @Override
  @Transactional(readOnly = true)
  public Integer getRecoveryCodeCount(AccountNo accountNo) {
    return recoveryCodeRepository.countByAccountNo(accountNo);
  }

  /**
   * リカバリーコードを再発行する
   *
   * <p>未使用のコードはすべて無効にする（紛失・漏洩したコードを使えなくするため）。 アクセストークンを盗んだ攻撃者に復旧手段を握られないよう、パスキーによる本人確認を必須とする
   *
   * @param accountNo アカウント番号（セッション由来）
   * @param reauthenticationResponseJson 本人確認用の認証レスポンス
   * @return 発行したリカバリーコード
   * @throws GalleryException 本人確認に失敗した場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public RecoveryCodeList regenerateRecoveryCodes(
      AccountNo accountNo, PasskeyResponseJson reauthenticationResponseJson)
      throws GalleryException {
    verifyReauthentication(
        accountNo, accountRepository.getByAccountNo(accountNo), reauthenticationResponseJson);

    recoveryCodeRepository.deleteByAccountNo(accountNo);
    return issueRecoveryCodes(accountNo);
  }

  /**
   * アカウントのパスキー用ユーザーハンドルを取得する。未発行であれば発行する
   *
   * <p>同一アカウントで同時に発行された場合でも、先に登録された1件に揃える
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
   * パスキーによる本人確認（再認証）を行う
   *
   * <p>パスキーの登録・削除、退会、リカバリーコードの再発行といった機微な操作の前に呼び出し、 アクセストークンの有効性だけでなく、
   * その場でのパスキーによる署名（生体認証・PIN）を要求することで、トークン漏洩・共有端末・誤操作による被害を限定する。
   *
   * <p>認証レスポンスは次をすべて満たす必要がある。
   *
   * <ul>
   *   <li>署名したパスキーが当該アカウントのものであること
   *   <li>チャレンジが{@link #createReauthenticationOptions}で当該アカウントに発行され、未使用かつ有効期限内であること
   *       （ログイン用など別用途のチャレンジは使えない）
   *   <li>署名・origin・RP ID・ユーザー検証・署名カウンタの検証を通ること
   * </ul>
   *
   * <p>失敗回数は{@link ReauthenticationThrottle}（アカウント単位のインメモリカウンタ）で数え、
   * 上限に達したアカウントは一定時間ロックアウトする。加えて、ログイン失敗回数の上限到達 （{@code auth.login.failCount}）
   * または管理者ロック中のアカウントも本人確認を通さない。本人確認に成功したら失敗カウンタをリセットする。
   *
   * @param accountNo アカウント番号
   * @param accountModel 当該アカウントの{@link AccountModel}（取得済み。null可）
   * @param reauthenticationResponseJson 本人確認用の認証レスポンス
   * @throws GalleryException 本人確認に失敗した場合、またはロック中の場合
   */
  private void verifyReauthentication(
      AccountNo accountNo,
      AccountModel accountModel,
      PasskeyResponseJson reauthenticationResponseJson)
      throws GalleryException {
    boolean throttleLockedOut = reauthenticationThrottle.isLockedOut(accountNo.value());
    if (throttleLockedOut || (accountModel != null && isReauthLocked(accountModel))) {
      if (throttleLockedOut) {
        // ロックアウト中の試行でも直近失敗時刻を更新し、ロックアウトをスライディングウィンドウにする
        // （攻撃者がロックアウト中に叩き続けても解除時刻が伸びないのを防ぐ）
        reauthenticationThrottle.recordFailure(accountNo.value());
      }
      log.info(
          "Re-authentication blocked because the account is locked out. (accountNo: {})",
          accountNo.value());
      throw ErrorEnum.REAUTHENTICATION_FAILED.toException();
    }

    try {
      PasskeyAssertionModel assertion =
          webAuthnVerifier.readAuthentication(reauthenticationResponseJson);

      // 同一パスキーによる同時認証を行ロックで直列化し、署名カウンタの検証と更新の間の競合を防ぐ
      PasskeyCredentialModel credential =
          passkeyCredentialRepository.findByCredentialIdForUpdate(assertion.getCredentialId());
      if (credential == null || !accountNo.equals(credential.getAccountNo())) {
        throw new PasskeyVerificationException("Passkey does not belong to this account");
      }

      // チャレンジは取得と同時に削除する（1度きり）。未発行・使用済み・期限切れ・他アカウント向けはここで弾く
      WebAuthnChallengeModel storedChallenge =
          webAuthnChallengeRepository.consumeForReauthentication(assertion.getChallenge());
      if (storedChallenge == null || !accountNo.equals(storedChallenge.getAccountNo())) {
        throw new PasskeyVerificationException(
            "Passkey re-authentication challenge is not available for this account");
      }

      PasskeyAssertionResultModel result =
          webAuthnVerifier.verifyAuthentication(
              reauthenticationResponseJson, assertion.getChallenge(), credential);
      passkeyCredentialRepository.updateAuthenticationResult(
          credential.withAuthenticationResult(result, new LastUsedAt(OffsetDateTime.now(clock))));
    } catch (PasskeyVerificationException e) {
      reauthenticationThrottle.recordFailure(accountNo.value());
      log.info(
          "Passkey re-authentication failed. (accountNo: {}, reason: {})",
          accountNo.value(),
          e.getMessage());
      throw ErrorEnum.REAUTHENTICATION_FAILED.toException();
    }

    reauthenticationThrottle.reset(accountNo.value());
  }

  /**
   * リカバリーコードを発行して保存する
   *
   * @param accountNo アカウント番号
   * @return 発行したリカバリーコード（平文）
   */
  private RecoveryCodeList issueRecoveryCodes(AccountNo accountNo) {
    RecoveryCodeList recoveryCodes =
        RecoveryCodeList.of(
            IntStream.range(0, Consts.RECOVERY_CODE_COUNT)
                .mapToObj(i -> RecoveryCode.generate(SECURE_RANDOM))
                .toList());
    recoveryCodeRepository.saveAll(accountNo, recoveryCodes);
    return recoveryCodes;
  }

  /**
   * WebAuthnチャレンジの有効期限（現在時刻＋{@code app.webauthn.challenge-timeout-seconds}）を求める
   *
   * @return {@link ExpiresAt}
   */
  private ExpiresAt challengeExpiresAt() {
    return new ExpiresAt(
        OffsetDateTime.now(clock).plusSeconds(webAuthnConfig.getChallengeTimeoutSeconds()));
  }

  /**
   * クライアントへ渡すWebAuthnのタイムアウト（ミリ秒）を求める
   *
   * @return {@link PasskeyTimeoutMillis}
   */
  private PasskeyTimeoutMillis challengeTimeoutMillis() {
    return new PasskeyTimeoutMillis(webAuthnConfig.getChallengeTimeoutSeconds() * 1000L);
  }

  /**
   * アカウントが再認証を通せないロック状態（ログイン失敗回数の上限到達、または管理者ロック）かどうかを判定する
   *
   * <p>管理者ロック済み・ログイン不能のアカウントは、通常は{@link
   * com.web.gallery.infrastructure.web.JwtAuthenticationFilter}が
   * アクセストークン検証時点で認証を拒否するため、この経路には到達しない。ただしフィルタの プリンシパルキャッシュ（{@code
   * app.auth.principal-cache-ttl-millis}）の反映猶予中や、ロック直前に
   * 発行され失効前のアクセストークンが残っているケースに備えた保険として、ここでも明示的に弾く。
   *
   * @param accountModel {@link AccountModel}
   * @return ロック状態の場合true
   */
  private boolean isReauthLocked(AccountModel accountModel) {
    boolean adminLocked =
        accountModel.getIsAdminLocked() != null
            && Boolean.TRUE.equals(accountModel.getIsAdminLocked().value());
    boolean failCountLocked =
        accountModel.getLoginFailureCount() != null
            && accountModel.getLoginFailureCount().value() >= loginConfig.getFailCount();
    return adminLocked || failCountLocked;
  }

  /**
   * 出身・在住の都道府県区分コードが、区分マスタに実在する（または未設定）ことを検証する
   *
   * <p>リクエストDTOでは形式（英数字20文字以内）しか検証できず、存在しないコードを保存すると 一覧・詳細画面での表示崩れやデータ不整合を招くため、Service層で実在チェックを行う。
   * どちらのコードも未指定（＝変更なし）の場合は区分マスタを参照しない。
   *
   * @param accountModel {@link AccountModel}
   * @throws GalleryException いずれかのコードが実在しない場合
   */
  private void validatePrefectureCodes(AccountModel accountModel) throws GalleryException {
    String birthplaceCode =
        accountModel.getBirthplacePrefectureKbnCode() != null
            ? accountModel.getBirthplacePrefectureKbnCode().value()
            : null;
    String residentCode =
        accountModel.getResidentPrefectureKbnCode() != null
            ? accountModel.getResidentPrefectureKbnCode().value()
            : null;
    if (birthplaceCode == null && residentCode == null) {
      return;
    }

    KbnMstModelList prefectureList = kbnMstRepository.get(new KbnClassCode(Consts.PREFECTURE));
    if (!isValidPrefectureCode(prefectureList, birthplaceCode)
        || !isValidPrefectureCode(prefectureList, residentCode)) {
      throw ErrorEnum.INVALID_INPUT.toException();
    }
  }

  /**
   * 都道府県区分コードが有効（null・未設定「none」・区分マスタに実在）かどうかを判定する
   *
   * @param prefectureList 区分マスタから取得した都道府県の一覧
   * @param code 検証対象の都道府県区分コード（null可）
   * @return 有効な場合true
   */
  private boolean isValidPrefectureCode(KbnMstModelList prefectureList, String code) {
    if (code == null || Consts.STRING_NONE.equals(code)) {
      return true;
    }
    return prefectureList.stream()
        .anyMatch(kbnMstModel -> kbnMstModel.getKbnCode().value().equals(code));
  }

  /**
   * 写真ファイルディレクトリの物理削除をトランザクションのコミット後に遅延実行する
   *
   * <p>トランザクション内で先にファイルを消すと、後続処理の失敗でDBがロールバックされたときに 「アカウント・写真レコードはあるが実体ファイルが無い」不整合が残る。
   * 削除自体の失敗はログ出力に留める。トランザクションが無い場合は即時削除する
   *
   * @param directoryPath 削除対象の写真ディレクトリパス
   */
  private void deletePhotoDirectoryAfterCommit(ImageFilePath directoryPath) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              deleteQuietly(directoryPath);
            }
          });
    } else {
      deleteQuietly(directoryPath);
    }
  }

  /**
   * 写真ファイル（ディレクトリ）を削除する。失敗しても例外を伝播させずログ出力に留める
   *
   * @param directoryPath 削除対象の写真ディレクトリパス
   */
  private void deleteQuietly(ImageFilePath directoryPath) {
    try {
      fileRepository.deleteByPrefix(directoryPath);
    } catch (RuntimeException e) {
      log.warn(
          "Failed to delete photo directory after commit. (directoryPath: {})",
          directoryPath.value(),
          e);
    }
  }

  /**
   * 認証成功
   *
   * @param event {@link AuthenticationSuccessEvent}
   * @throws GalleryException 更新に失敗した場合
   */
  @EventListener
  // ログイン成功時は呼び出し元（AuthServiceImplのログイン処理）のトランザクションに参加する
  @Transactional(rollbackFor = GalleryException.class)
  public void handle(AuthenticationSuccessEvent event) throws GalleryException {
    AccountModel accountModel =
        accountRepository.getByAccountId(new AccountId(event.getAuthentication().getName()));

    if (Objects.isNull(accountModel)) {
      return;
    }
    accountRepository.updateLoginFailureCount(
        AccountModel.forLoginSuccess(accountModel.getAccountNo(), clock));
    recordLoginHistory(event.getAuthentication(), accountModel.getAccountNo());
  }

  /**
   * ログイン履歴を記録する（ログイン成功時のみ）
   *
   * <p>存在しないアカウントIDへのログイン試行は対象外（呼び出し元でaccountModelがnullでないことを確認済み）。
   * IPアドレス・ログイン方式は、AuthServiceImpl#loginWithPasskey（パスキー）・AuthServiceImpl#loginWithRecoveryCode（リカバリーコード）が
   * Authenticationのdetailsに設定したものを取り出す
   *
   * @param authentication 認証結果（{@link LoginContextModel}をdetailsに保持する）
   * @param accountNo アカウント番号
   */
  private void recordLoginHistory(Authentication authentication, AccountNo accountNo) {
    if (!(authentication.getDetails() instanceof LoginContextModel loginContext)) {
      return;
    }
    IpGeoLocation geoLocation = geoIpResolver.resolve(loginContext.getIpAddress());
    loginHistoryRepository.save(
        LoginHistoryModel.of(
            accountNo, loginContext.getIpAddress(), geoLocation, loginContext.getLoginMethodKbn()));
  }
}
