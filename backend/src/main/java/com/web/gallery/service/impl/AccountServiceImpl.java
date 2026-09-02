package com.web.gallery.service.impl;

import java.time.Clock;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.web.gallery.AccountPrincipal;
import com.web.gallery.aggregate.Account;
import com.web.gallery.config.AccountConfig;
import com.web.gallery.config.LoginConfig;
import com.web.gallery.config.PhotoConfig;
import com.web.gallery.constant.Consts;
import com.web.gallery.constant.MessageConst;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.KbnClassCode;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.event.AccountDeletedEvent;
import com.web.gallery.event.AccountLockedEvent;
import com.web.gallery.event.AccountRegisteredEvent;
import com.web.gallery.event.AccountUnlockedEvent;
import com.web.gallery.event.AccountUpdatedEvent;
import com.web.gallery.event.PhotoDeletedEvent;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.exception.GalleryException;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * アカウントに関するビジネスロジックを行うServiceの実装クラス
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements UserDetailsService, AccountService {

	private final AccountRepository accountRepository;
	private final AccountAggregateRepository accountAggregateRepository;
	private final FileRepository fileRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final KbnMstRepository kbnMstRepository;
	private final LoginConfig loginConfig;
	private final PhotoConfig photoConfig;
	private final AccountConfig accountConfig;
	private final Clock clock;
	private final ApplicationEventPublisher applicationEventPublisher;

	/**
	 * アカウントIDからアカウント情報の存在を確認する
	 *
	 * @param	username					アカウントID
	 * @return								{@link UserDetails}
	 * @throws	UsernameNotFoundException	ユーザーが存在しない場合
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
	 * @param	accountModel		{@link AccountModel}
	 * @return						登録に成功した場合、true
	 * @throws	GalleryException	登録に失敗した場合
	 */
	@Override
	@Transactional(rollbackFor = GalleryException.class)
	public Boolean registAccount(AccountModel accountModel) throws GalleryException {
		validatePrefectureCodes(accountModel);

		Boolean isExist = accountRepository.isExistAccount(accountModel.getAccountId());
		if(!isExist) {
			accountRepository.regist(accountModel);
			applicationEventPublisher.publishEvent(new AccountRegisteredEvent(accountModel.getAccountId()));
		}
		return !isExist;
	}

	/**
	 * アカウントを更新する<p>
	 * パスワードが変更された場合は、当該アカウントのリフレッシュトークンをすべて失効させ、
	 * 全セッションでの再認証を強制する（トークン漏洩時の被害を限定するため）
	 *
	 * @param	accountModel		{@link AccountModel}
	 * @return						アカウントIDが重複しており更新をスキップした場合、true
	 * @throws GalleryException	更新に失敗した場合
	 */
	@Override
	@Transactional(rollbackFor = GalleryException.class)
	public Boolean updateAccount(AccountModel accountModel) throws GalleryException {
		validatePrefectureCodes(accountModel);

		Boolean isExist = accountRepository.isExistAccount(accountModel.getAccountNo(), accountModel.getAccountId());
		if(!isExist) {
			accountRepository.update(accountModel);
			if (accountModel.getPassword() != null) {
				refreshTokenRepository.revokeAllByAccountNo(accountModel.getAccountNo());
			}
			applicationEventPublisher.publishEvent(new AccountUpdatedEvent(accountModel.getAccountNo(), accountModel.getAccountId()));
		}
		return isExist;
	}

	/**
	 * アカウントIDからアカウント情報を取得する
	 *
	 * @param	accountId	アカウントID
	 * @return				{@link AccountModel}
	 */
	@Override
	@Transactional(readOnly = true)
	public AccountModel getAccountById(AccountId accountId) {
		return accountRepository.getByAccountId(accountId);
	}

	/**
	 * アカウントの一覧を、ページング情報に従い取得する
	 *
	 * @param	accountListGetModel	{@link AccountListGetModel}
	 * @return						{@link AccountPageModel}
	 */
	@Override
	@Transactional(readOnly = true)
	public AccountPageModel getAccountList(AccountListGetModel accountListGetModel) {
		AccountGetModel accountGetModel = AccountGetModel.of(accountListGetModel, accountConfig.getAccountCountPerPage());
		AccountPageModel accountPageModel = accountRepository.getAccountList(accountGetModel);
		return AccountPageModel.of(accountPageModel.getAccountModelList().sortByAccountId(), accountPageModel.getIsLast());
	}

	/**
	 * 管理者用：削除済みを含む全アカウントの一覧を、ページング情報に従い取得する
	 *
	 * @param	accountListGetModel	{@link AccountListGetModel}
	 * @return						{@link AccountPageModel}
	 */
	@Override
	@Transactional(readOnly = true)
	public AccountPageModel getAccountListForAdmin(AccountListGetModel accountListGetModel) {
		AccountGetModel accountGetModel = AccountGetModel.of(accountListGetModel, accountConfig.getAccountCountPerPage());
		AccountPageModel accountPageModel = accountRepository.getAccountListForAdmin(accountGetModel);
		return AccountPageModel.of(accountPageModel.getAccountModelList().sortByAccountId(), accountPageModel.getIsLast());
	}

	/**
	 * 管理者用：アカウントのロックを解除する（管理者ロックフラグを解除し、ログイン失敗回数も0にリセット）
	 *
	 * @param	accountNo			アカウント番号
	 * @throws	GalleryException	更新に失敗した場合
	 */
	@Override
	@Transactional(rollbackFor = GalleryException.class)
	public void unlockAccount(AccountNo accountNo) throws GalleryException {
		// 管理者ロックとログイン失敗回数の両方を解除する
		accountRepository.updateLoginFailureCount(AccountModel.forAdminUnlock(accountNo.value()));
		applicationEventPublisher.publishEvent(new AccountUnlockedEvent(accountNo));
	}

	/**
	 * 管理者用：アカウントを強制ロックする<p>
	 * 管理者ロックフラグを立てる（ログイン失敗回数による自動解除の対象外）。
	 * あわせてログイン失敗回数も上限値に設定する（管理画面の表示・判定と整合させるため）
	 *
	 * @param	accountNo			アカウント番号
	 * @throws	GalleryException	更新に失敗した場合
	 */
	@Override
	@Transactional(rollbackFor = GalleryException.class)
	public void lockAccount(AccountNo accountNo) throws GalleryException {
		accountRepository.updateLoginFailureCount(AccountModel.forLock(accountNo.value(), loginConfig.getFailCount()));
		applicationEventPublisher.publishEvent(new AccountLockedEvent(accountNo));
	}

	/**
	 * アカウントを削除する
	 *
	 * @param	accountNo	アカウント番号
	 * @param	accountId	アカウントID
	 */
	@Override
	@Transactional
	public void deleteAccount(AccountNo accountNo, AccountId accountId) {
		Account account = Account.forDelete(accountNo);
		accountAggregateRepository.delete(account);

		// 写真ファイルのディレクトリ削除はDBコミット確定後に行う（ロールバック時の不整合を防ぐ）
		deletePhotoDirectoryAfterCommit(new ImageFilePath(photoConfig.getOutputPath() + accountId.value() + "/"));

		for(PhotoNo photoNo : account.getDeletedPhotoNoList()) {
			applicationEventPublisher.publishEvent(new PhotoDeletedEvent(accountNo, photoNo));
		}
		applicationEventPublisher.publishEvent(new AccountDeletedEvent(accountNo, accountId));
	}

	/**
	 * 出身・在住の都道府県区分コードが、区分マスタに実在する（または未設定）ことを検証する<p>
	 * リクエストDTOでは形式（英数字20文字以内）しか検証できず、存在しないコードを保存すると
	 * 一覧・詳細画面での表示崩れやデータ不整合を招くため、Service層で実在チェックを行う。
	 * どちらのコードも未指定（＝変更なし）の場合は区分マスタを参照しない。
	 *
	 * @param	accountModel		{@link AccountModel}
	 * @throws	GalleryException	いずれかのコードが実在しない場合
	 */
	private void validatePrefectureCodes(AccountModel accountModel) throws GalleryException {
		String birthplaceCode = accountModel.getBirthplacePrefectureKbnCode() != null
				? accountModel.getBirthplacePrefectureKbnCode().value() : null;
		String residentCode = accountModel.getResidentPrefectureKbnCode() != null
				? accountModel.getResidentPrefectureKbnCode().value() : null;
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
	 * @param	prefectureList	区分マスタから取得した都道府県の一覧
	 * @param	code			検証対象の都道府県区分コード（null可）
	 * @return					有効な場合true
	 */
	private boolean isValidPrefectureCode(KbnMstModelList prefectureList, String code) {
		if (code == null || Consts.STRING_NONE.equals(code)) {
			return true;
		}
		return prefectureList.stream()
				.anyMatch(kbnMstModel -> kbnMstModel.getKbnCode().value().equals(code));
	}

	/**
	 * 写真ファイルディレクトリの物理削除をトランザクションのコミット後に遅延実行する<p>
	 * トランザクション内で先にファイルを消すと、後続処理の失敗でDBがロールバックされたときに
	 * 「アカウント・写真レコードはあるが実体ファイルが無い」不整合が残る。
	 * 削除自体の失敗はログ出力に留める。トランザクションが無い場合は即時削除する
	 *
	 * @param	directoryPath	削除対象の写真ディレクトリパス
	 */
	private void deletePhotoDirectoryAfterCommit(ImageFilePath directoryPath) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
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
	 * @param	directoryPath	削除対象の写真ディレクトリパス
	 */
	private void deleteQuietly(ImageFilePath directoryPath) {
		try {
			fileRepository.delete(directoryPath);
		} catch (RuntimeException e) {
			log.warn("Failed to delete photo directory after commit. (directoryPath: {})", directoryPath.value(), e);
		}
	}

	/**
	 * 認証成功
	 *
	 * @param	event				{@link AuthenticationSuccessEvent}
	 * @throws	GalleryException	更新に失敗した場合
	 */
	@EventListener
	// ログイン成功時は呼び出し元login()のトランザクションに参加する。
	// 認証失敗イベントと異なり、成功時はlogin()がロールバックされないため独立トランザクションは不要
	@Transactional(rollbackFor = GalleryException.class)
	public void handle(AuthenticationSuccessEvent event) throws GalleryException {
		AccountModel accountModel = accountRepository.getByAccountId(new AccountId(event.getAuthentication().getName()));

		if (Objects.isNull(accountModel)) {
			return;
		}
		accountRepository.updateLoginFailureCount(AccountModel.forLoginSuccess(accountModel.getAccountNo(), clock));
	}

	/**
	 * 認証失敗
	 *
	 * @param	event				{@link AuthenticationFailureBadCredentialsEvent}
	 * @throws	GalleryException	更新に失敗した場合
	 */
	@EventListener
	// authenticationManager.authenticate()が投げるBadCredentialsExceptionは呼び出し元のlogin()の
	// トランザクション境界まで伝播しロールバックされるため、REQUIRES_NEWで独立してコミットする
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = GalleryException.class)
	public void handle(AuthenticationFailureBadCredentialsEvent event) throws GalleryException {
		AccountModel accountModel = accountRepository.getByAccountId(new AccountId(event.getAuthentication().getName()));

		if(!Objects.isNull(accountModel)) {
			accountRepository.incrementLoginFailureCount(accountModel.getAccountNo());
		}
	}
}
