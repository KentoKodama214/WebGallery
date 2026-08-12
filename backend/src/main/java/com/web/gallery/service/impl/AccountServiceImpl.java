package com.web.gallery.service.impl;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web.gallery.AccountPrincipal;
import com.web.gallery.config.LoginConfig;
import com.web.gallery.config.PhotoConfig;
import com.web.gallery.constant.Consts;
import com.web.gallery.constant.MessageConst;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.account.LastLoginDatetime;
import com.web.gallery.domain.account.LoginFailureCount;
import com.web.gallery.entity.PhotoFavorite;
import com.web.gallery.entity.PhotoMst;
import com.web.gallery.entity.PhotoTagMst;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.mapper.PhotoFavoriteMapper;
import com.web.gallery.mapper.PhotoMstMapper;
import com.web.gallery.mapper.PhotoTagMstMapper;
import com.web.gallery.model.AccountModel;
import com.web.gallery.repository.AccountRepository;
import com.web.gallery.repository.FileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * アカウントに関するビジネスロジックを行うServiceの実装クラス
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements UserDetailsService {

	private final AccountRepository accountRepository;
	private final FileRepository fileRepository;
	private final PhotoFavoriteMapper photoFavoriteMapper;
	private final PhotoTagMstMapper photoTagMstMapper;
	private final PhotoMstMapper photoMstMapper;
	private final LoginConfig loginConfig;
	private final PhotoConfig photoConfig;

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
		AccountModel accountModel = accountRepository.getByAccountId(username);

		if (Objects.isNull(accountModel)) {
			log.info("User not found. (username: {})", username);
			throw new UsernameNotFoundException(MessageConst.USER_NOT_FOUND);
		}
		return new AccountPrincipal(accountModel, loginConfig.getFailCount());
	}

	/**
	 * アカウントを新規登録する
	 *
	 * @param	accountModel			{@link AccountModel}
	 * @return							登録に成功した場合、true
	 * @throws	RegistFailureException	登録に失敗した場合
	 */
	@Transactional
	public Boolean registAccount(AccountModel accountModel) throws RegistFailureException {
		Boolean isExist = accountRepository.isExistAccount(null, accountModel.getAccountId().value());
		if(!isExist) accountRepository.regist(accountModel);
		return !isExist;
	}

	/**
	 * アカウントを更新する
	 *
	 * @param	accountModel			{@link AccountModel}
	 * @return							更新に成功した場合、true
	 * @throws UpdateFailureException	更新に失敗した場合
	 */
	@Transactional
	public Boolean updateAccount(AccountModel accountModel) throws UpdateFailureException {
		Boolean isExist = accountRepository.isExistAccount(accountModel.getAccountNo().value(), accountModel.getAccountId().value());
		if(!isExist) accountRepository.update(accountModel);
		return isExist;
	}

	/**
	 * アカウントIDからアカウント情報を取得する
	 *
	 * @param	accountId	アカウントID
	 * @return				{@link AccountModel}
	 */
	@Transactional(readOnly = true)
	public AccountModel getAccountById(String accountId) {
		return accountRepository.getByAccountId(accountId);
	}

	/**
	 * アカウントの一覧を取得する
	 *
	 * @return	{@link AccountModel}
	 */
	@Transactional(readOnly = true)
	public List<AccountModel> getAccountList() {
		return accountRepository.getAccountList().stream().sorted(Comparator.comparing(m -> m.getAccountId().value())).toList();
	}

	/**
	 * 管理者用：削除済みを含む全アカウントの一覧を取得する
	 *
	 * @return	{@link AccountModel}
	 */
	@Transactional(readOnly = true)
	public List<AccountModel> getAccountListForAdmin() {
		return accountRepository.getAccountListAll().stream().sorted(Comparator.comparing(m -> m.getAccountId().value())).toList();
	}

	/**
	 * 管理者用：アカウントのロックを解除する（ログイン失敗回数を0にリセット）
	 *
	 * @param	accountNo				アカウント番号
	 * @throws	UpdateFailureException	更新に失敗した場合
	 */
	@Transactional
	public void unlockAccount(Long accountNo) throws UpdateFailureException {
		AccountModel updateModel = AccountModel.builder()
				.accountNo(new AccountNo(accountNo))
				.loginFailureCount(new LoginFailureCount(0))
				.build();
		accountRepository.updateLoginFailureCount(updateModel);
	}

	/**
	 * 管理者用：アカウントを強制ロックする（ログイン失敗回数を上限超過に設定）
	 *
	 * @param	accountNo				アカウント番号
	 * @throws	UpdateFailureException	更新に失敗した場合
	 */
	@Transactional
	public void lockAccount(Long accountNo) throws UpdateFailureException {
		AccountModel updateModel = AccountModel.builder()
				.accountNo(new AccountNo(accountNo))
				.loginFailureCount(new LoginFailureCount(loginConfig.getFailCount()))
				.build();
		accountRepository.updateLoginFailureCount(updateModel);
	}

	/**
	 * アカウントを削除する
	 *
	 * @param	accountNo	アカウント番号
	 * @param	accountId	アカウントID
	 */
	@Transactional
	public void deleteAccount(Long accountNo, String accountId) {
		AccountNo accountNoVo = new AccountNo(accountNo);

		// 自分が登録したお気に入りを削除
		photoFavoriteMapper.delete(PhotoFavorite.builder().accountNo(accountNoVo).build());

		// 自分の写真に対する他人のお気に入りを削除
		photoFavoriteMapper.delete(PhotoFavorite.builder().favoritePhotoAccountNo(accountNoVo).build());

		// 写真タグを削除
		photoTagMstMapper.delete(PhotoTagMst.builder().accountNo(accountNoVo).build());

		// 写真マスタを物理削除
		photoMstMapper.delete(PhotoMst.builder().accountNo(accountNoVo).build());

		// アカウントを物理削除
		accountRepository.delete(accountNo);

		// 写真ファイルのディレクトリを削除
		fileRepository.delete(photoConfig.getOutputPath() + accountId + "/");
	}

	/**
	 * 認証成功
	 *
	 * @param	event					{@link AuthenticationSuccessEvent}
	 * @throws	UpdateFailureException	更新に失敗した場合
	 */
	@EventListener
	@Transactional
	public void handle(AuthenticationSuccessEvent event) throws UpdateFailureException {
		AccountModel accountModel = accountRepository.getByAccountId(event.getAuthentication().getName());

		AccountModel updateModel = AccountModel.builder()
				.accountNo(accountModel.getAccountNo())
				.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.now(Consts.JST)))
				.loginFailureCount(new LoginFailureCount(0))
				.build();
		accountRepository.updateLoginFailureCount(updateModel);
	}

	/**
	 * 認証失敗
	 *
	 * @param	event					{@link AuthenticationFailureBadCredentialsEvent}
	 * @throws	UpdateFailureException	更新に失敗した場合
	 */
	@EventListener
	@Transactional
	public void handle(AuthenticationFailureBadCredentialsEvent event) throws UpdateFailureException {
		AccountModel accountModel = accountRepository.getByAccountId(event.getAuthentication().getName());

		if(!Objects.isNull(accountModel)) {
			AccountModel updateModel = AccountModel.builder()
					.accountNo(accountModel.getAccountNo())
					.loginFailureCount(new LoginFailureCount(accountModel.getLoginFailureCount().value() + 1))
					.build();
			accountRepository.updateLoginFailureCount(updateModel);
		}
	}
}
