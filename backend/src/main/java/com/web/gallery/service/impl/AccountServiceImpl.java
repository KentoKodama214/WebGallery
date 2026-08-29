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
import org.springframework.transaction.annotation.Transactional;

import com.web.gallery.AccountPrincipal;
import com.web.gallery.config.AccountConfig;
import com.web.gallery.config.LoginConfig;
import com.web.gallery.config.PhotoConfig;
import com.web.gallery.constant.MessageConst;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.event.AccountDeletedEvent;
import com.web.gallery.event.AccountLockedEvent;
import com.web.gallery.event.AccountRegisteredEvent;
import com.web.gallery.event.AccountUnlockedEvent;
import com.web.gallery.event.AccountUpdatedEvent;
import com.web.gallery.event.PhotoDeletedEvent;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.model.AccountGetModel;
import com.web.gallery.model.AccountListGetModel;
import com.web.gallery.model.AccountModel;
import com.web.gallery.model.AccountModelList;
import com.web.gallery.model.AccountPageModel;
import com.web.gallery.model.PhotoNoList;
import com.web.gallery.repository.AccountRepository;
import com.web.gallery.repository.FileRepository;
import com.web.gallery.repository.PhotoFavoriteRepository;
import com.web.gallery.repository.PhotoMstRepository;
import com.web.gallery.repository.PhotoTagMstRepository;
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
	private final FileRepository fileRepository;
	private final PhotoFavoriteRepository photoFavoriteRepository;
	private final PhotoTagMstRepository photoTagMstRepository;
	private final PhotoMstRepository photoMstRepository;
	private final RefreshTokenRepository refreshTokenRepository;
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
		Boolean isExist = accountRepository.isExistAccount(accountModel.getAccountId());
		if(!isExist) {
			accountRepository.regist(accountModel);
			applicationEventPublisher.publishEvent(new AccountRegisteredEvent(accountModel.getAccountId()));
		}
		return !isExist;
	}

	/**
	 * アカウントを更新する
	 *
	 * @param	accountModel		{@link AccountModel}
	 * @return						アカウントIDが重複しており更新をスキップした場合、true
	 * @throws GalleryException	更新に失敗した場合
	 */
	@Override
	@Transactional(rollbackFor = GalleryException.class)
	public Boolean updateAccount(AccountModel accountModel) throws GalleryException {
		Boolean isExist = accountRepository.isExistAccount(accountModel.getAccountNo(), accountModel.getAccountId());
		if(!isExist) {
			accountRepository.update(accountModel);
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
	 * 管理者用：削除済みを含む全アカウントの一覧を取得する
	 *
	 * @return	{@link AccountModelList}
	 */
	@Override
	@Transactional(readOnly = true)
	public AccountModelList getAccountListForAdmin() {
		return accountRepository.getAccountListAll().sortByAccountId();
	}

	/**
	 * 管理者用：アカウントのロックを解除する（ログイン失敗回数を0にリセット）
	 *
	 * @param	accountNo			アカウント番号
	 * @throws	GalleryException	更新に失敗した場合
	 */
	@Override
	@Transactional(rollbackFor = GalleryException.class)
	public void unlockAccount(AccountNo accountNo) throws GalleryException {
		accountRepository.updateLoginFailureCount(AccountModel.forUnlock(accountNo.value()));
		applicationEventPublisher.publishEvent(new AccountUnlockedEvent(accountNo));
	}

	/**
	 * 管理者用：アカウントを強制ロックする（ログイン失敗回数を上限超過に設定）
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
		// 自分が登録したお気に入りを削除
		photoFavoriteRepository.deleteByAccountNo(accountNo);

		// 自分の写真に対する他人のお気に入りを削除
		photoFavoriteRepository.deleteByFavoritePhotoAccountNo(accountNo);

		// 写真タグを削除
		photoTagMstRepository.deleteByAccountNo(accountNo);

		// 写真マスタを物理削除し、削除時点で未削除だった写真番号を取得
		// SELECTとDELETEの間のTOCTOUギャップを無くすため単一SQLでアトミックに実施し、
		// 既に論理削除済みだった写真はイベントの重複発行を避けるため対象から除外する
		PhotoNoList deletedPhotoNoList = photoMstRepository.deleteAndGetUndeletedPhotoNosByAccountNo(accountNo);

		// リフレッシュトークンを失効
		refreshTokenRepository.revokeAllByAccountNo(accountNo);

		// アカウントを物理削除
		accountRepository.delete(accountNo);

		// 写真ファイルのディレクトリを削除
		fileRepository.delete(new ImageFilePath(photoConfig.getOutputPath() + accountId.value() + "/"));

		for(PhotoNo photoNo : deletedPhotoNoList) {
			applicationEventPublisher.publishEvent(new PhotoDeletedEvent(accountNo, photoNo));
		}
		applicationEventPublisher.publishEvent(new AccountDeletedEvent(accountNo, accountId));
	}

	/**
	 * 認証成功
	 *
	 * @param	event				{@link AuthenticationSuccessEvent}
	 * @throws	GalleryException	更新に失敗した場合
	 */
	@EventListener
	@Transactional(rollbackFor = GalleryException.class)
	public void handle(AuthenticationSuccessEvent event) throws GalleryException {
		AccountModel accountModel = accountRepository.getByAccountId(new AccountId(event.getAuthentication().getName()));

		accountRepository.updateLoginFailureCount(AccountModel.forLoginSuccess(accountModel.getAccountNo(), clock));
	}

	/**
	 * 認証失敗
	 *
	 * @param	event				{@link AuthenticationFailureBadCredentialsEvent}
	 * @throws	GalleryException	更新に失敗した場合
	 */
	@EventListener
	@Transactional(rollbackFor = GalleryException.class)
	public void handle(AuthenticationFailureBadCredentialsEvent event) throws GalleryException {
		AccountModel accountModel = accountRepository.getByAccountId(new AccountId(event.getAuthentication().getName()));

		if(!Objects.isNull(accountModel)) {
			accountRepository.updateLoginFailureCount(
					AccountModel.forLoginFailure(accountModel.getAccountNo(), accountModel.getLoginFailureCount()));
		}
	}
}
