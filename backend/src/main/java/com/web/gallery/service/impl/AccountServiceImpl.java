package com.web.gallery.service.impl;

import com.web.gallery.AccountPrincipal;
import com.web.gallery.aggregate.Account;
import com.web.gallery.config.AccountConfig;
import com.web.gallery.config.LoginConfig;
import com.web.gallery.config.PhotoConfig;
import com.web.gallery.constant.Consts;
import com.web.gallery.constant.MessageConst;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.account.Password;
import com.web.gallery.domain.common.KbnClassCode;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.event.AccountDeletedEvent;
import com.web.gallery.event.AccountLockedEvent;
import com.web.gallery.event.AccountRegisteredEvent;
import com.web.gallery.event.AccountUnlockedEvent;
import com.web.gallery.event.AccountUpdatedEvent;
import com.web.gallery.event.PhotoDeletedEvent;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.helper.ReauthenticationThrottle;
import com.web.gallery.model.AccountGetModel;
import com.web.gallery.model.AccountListGetModel;
import com.web.gallery.model.AccountModel;
import com.web.gallery.model.AccountPageModel;
import com.web.gallery.model.KbnMstModelList;
import com.web.gallery.repository.AccountAggregateRepository;
import com.web.gallery.repository.AccountRepository;
import com.web.gallery.repository.FileRepository;
import com.web.gallery.repository.KbnMstRepository;
import com.web.gallery.repository.RefreshTokenRepository;
import com.web.gallery.service.AccountService;
import java.time.Clock;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** アカウントに関するビジネスロジックを行うServiceの実装クラス */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements UserDetailsService, AccountService {

  /**
   * 現在のパスワード照合で、アカウント不在・パスワード未設定時にもBCrypt照合1回分の 応答時間を保つためのダミーハッシュ（照合結果は使用しない）。
   *
   * <p>accountNoは常に呼び出し元自身のセッション由来のため他人の列挙には使えないが、 応答時間からレコード整合性の破損を推測される余地もなくすための保険。
   * 値は公開されているBCryptのテストベクタ（平文 {@code "password"}）。
   */
  private static final String DUMMY_PASSWORD_HASH =
      "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

  private final AccountRepository accountRepository;
  private final AccountAggregateRepository accountAggregateRepository;
  private final FileRepository fileRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final KbnMstRepository kbnMstRepository;
  private final PasswordEncoder passwordEncoder;
  private final ReauthenticationThrottle reauthenticationThrottle;
  private final LoginConfig loginConfig;
  private final PhotoConfig photoConfig;
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
   * アカウントを新規登録する
   *
   * @param accountModel {@link AccountModel}
   * @return 登録に成功した場合、true
   * @throws GalleryException 登録に失敗した場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public Boolean registAccount(AccountModel accountModel) throws GalleryException {
    validatePrefectureCodes(accountModel);

    Boolean isExist = accountRepository.isExistAccount(accountModel.getAccountId());
    if (!isExist) {
      accountRepository.regist(accountModel);
      applicationEventPublisher.publishEvent(
          new AccountRegisteredEvent(accountModel.getAccountId()));
    }
    return !isExist;
  }

  /**
   * アカウントを更新する
   *
   * <p>パスワードまたはアカウントIDを変更する場合は、当該アカウントのリフレッシュトークンをすべて失効させ、
   * 全セッションでの再認証を強制する（トークン漏洩時の被害を限定し、アカウントID変更後に アクセストークンのsubjectが宙に浮くのを防ぐため）。
   * パスワード変更時は、失効に先立って{@code currentPassword}による本人確認を行う。
   *
   * @param accountModel {@link AccountModel}
   * @param currentPassword 現在のパスワード（パスワード変更時のみ必須）
   * @return アカウントIDが重複しており更新をスキップした場合、true
   * @throws GalleryException 更新に失敗した場合、または現在のパスワードが一致しない場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public Boolean updateAccount(AccountModel accountModel, Password currentPassword)
      throws GalleryException {
    validatePrefectureCodes(accountModel);

    // 変更後のアカウントIDが他アカウントと重複する場合は更新をスキップする。
    // この判定を先に行い、重複時は本人確認（BCrypt照合）を行わない
    Boolean isExist =
        accountRepository.isExistAccount(accountModel.getAccountNo(), accountModel.getAccountId());
    if (isExist) {
      return true;
    }

    AccountModel currentAccount = accountRepository.getByAccountNo(accountModel.getAccountNo());

    // パスワード変更時は本人確認（再認証）を行う
    boolean isPasswordChange = accountModel.getPassword() != null;
    if (isPasswordChange) {
      verifyCurrentPassword(accountModel.getAccountNo(), currentAccount, currentPassword);
    }

    // アカウントIDが変わると既存のアクセストークンのsubjectが解決不能になるため検知する
    boolean isAccountIdChanged =
        currentAccount != null
            && currentAccount.getAccountId() != null
            && !currentAccount.getAccountId().value().equals(accountModel.getAccountId().value());

    accountRepository.update(accountModel);
    if (isPasswordChange || isAccountIdChanged) {
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
   * アカウントを削除する
   *
   * <p>{@code currentPassword}による本人確認（再認証）を行ったうえで物理削除する
   *
   * @param accountNo アカウント番号
   * @param accountId アカウントID
   * @param currentPassword 現在のパスワード
   * @throws GalleryException 現在のパスワードが一致しない場合
   */
  @Override
  @Transactional(rollbackFor = GalleryException.class)
  public void deleteAccount(AccountNo accountNo, AccountId accountId, Password currentPassword)
      throws GalleryException {
    verifyCurrentPassword(accountNo, accountRepository.getByAccountNo(accountNo), currentPassword);

    Account account = Account.forDelete(accountNo);
    accountAggregateRepository.delete(account);

    // 写真ファイルのディレクトリ削除はDBコミット確定後に行う（ロールバック時の不整合を防ぐ）
    deletePhotoDirectoryAfterCommit(
        new ImageFilePath(photoConfig.getOutputPath() + accountId.value() + "/"));

    for (PhotoNo photoNo : account.getDeletedPhotoNoList()) {
      applicationEventPublisher.publishEvent(new PhotoDeletedEvent(accountNo, photoNo));
    }
    applicationEventPublisher.publishEvent(new AccountDeletedEvent(accountNo, accountId));
  }

  /**
   * 現在のパスワードによる本人確認（再認証）を行う
   *
   * <p>パスワード変更・アカウント削除といった機微な操作の前に呼び出し、 アクセストークンの有効性だけでなく現在のパスワードの入力を要求することで、
   * トークン漏洩・共有端末・誤操作による被害を限定する。
   *
   * <p>アクセストークン漏洩時のオンライン総当たりを防ぐため、再認証の失敗回数を {@link ReauthenticationThrottle}（アカウント単位のインメモリカウンタ）で数え、
   * 上限に達したアカウントは一定時間ロックアウトする。加えて、ログイン失敗回数の上限到達 （{@code
   * auth.login.failCount}）または管理者ロック中のアカウントも本人確認を通さない。 本人確認に成功したら失敗カウンタをリセットする。
   *
   * @param accountNo アカウント番号
   * @param accountModel 当該アカウントの{@link AccountModel}（取得済み。null可・nullは不一致として扱う）
   * @param currentPassword 入力された現在のパスワード（null可。nullは不一致として扱う）
   * @throws GalleryException 現在のパスワードが未入力・不一致、またはロック中の場合
   */
  private void verifyCurrentPassword(
      AccountNo accountNo, AccountModel accountModel, Password currentPassword)
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
      throw ErrorEnum.CURRENT_PASSWORD_MISMATCH.toException();
    }

    // アカウント不在・パスワード未設定でも早期returnせず、BCrypt照合1回分のコストを必ず払う
    // （応答時間を一定に保ち、タイミングによる推測を防ぐ）
    boolean matches;
    if (currentPassword == null || accountModel == null || accountModel.getPassword() == null) {
      passwordEncoder.matches(
          currentPassword != null ? currentPassword.value() : "", DUMMY_PASSWORD_HASH);
      matches = false;
    } else {
      matches =
          passwordEncoder.matches(currentPassword.value(), accountModel.getPassword().value());
    }
    if (!matches) {
      reauthenticationThrottle.recordFailure(accountNo.value());
      log.info("Current password verification failed. (accountNo: {})", accountNo.value());
      throw ErrorEnum.CURRENT_PASSWORD_MISMATCH.toException();
    }

    reauthenticationThrottle.reset(accountNo.value());
  }

  /**
   * アカウントが再認証を通せないロック状態（ログイン失敗回数の上限到達、または管理者ロック）かどうかを判定する
   *
   * <p>管理者ロック済み・ログイン不能のアカウントは、通常は{@link com.web.gallery.config.JwtAuthenticationFilter}が
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
      fileRepository.delete(directoryPath);
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
  // ログイン成功時は呼び出し元login()のトランザクションに参加する。
  // 認証失敗イベントと異なり、成功時はlogin()がロールバックされないため独立トランザクションは不要
  @Transactional(rollbackFor = GalleryException.class)
  public void handle(AuthenticationSuccessEvent event) throws GalleryException {
    AccountModel accountModel =
        accountRepository.getByAccountId(new AccountId(event.getAuthentication().getName()));

    if (Objects.isNull(accountModel)) {
      return;
    }
    accountRepository.updateLoginFailureCount(
        AccountModel.forLoginSuccess(accountModel.getAccountNo(), clock));
  }

  /**
   * 認証失敗
   *
   * @param event {@link AuthenticationFailureBadCredentialsEvent}
   * @throws GalleryException 更新に失敗した場合
   */
  @EventListener
  // authenticationManager.authenticate()が投げるBadCredentialsExceptionは呼び出し元のlogin()の
  // トランザクション境界まで伝播しロールバックされるため、REQUIRES_NEWで独立してコミットする
  @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = GalleryException.class)
  public void handle(AuthenticationFailureBadCredentialsEvent event) throws GalleryException {
    AccountModel accountModel =
        accountRepository.getByAccountId(new AccountId(event.getAuthentication().getName()));

    if (!Objects.isNull(accountModel)) {
      accountRepository.incrementLoginFailureCount(accountModel.getAccountNo());
    }
  }
}
