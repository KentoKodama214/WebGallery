package com.web.gallery.repository.impl;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.entity.Account;
import com.web.gallery.entity.AccountCondition;
import com.web.gallery.entity.AccountUpdateTarget;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.mapper.AccountMapper;
import com.web.gallery.model.AccountModel;
import com.web.gallery.model.AccountModelList;
import com.web.gallery.repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * アカウントデータを永続化するRepositoryの実装クラス
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepository {

	private final AccountMapper accountMapper;
	private final PasswordEncoder passwordEncoder;

	/**
	 * Accountテーブルで該当するレコードを取得する
	 *
	 * @param	accountNo		アカウント番号
	 * @return	AccountModel	{@link AccountModel}<p>
	 * 							取得できない場合はnullを返す
	 */
	@Override
	public AccountModel getByAccountNo(AccountNo accountNo) {
		List<Account> accountList = accountMapper.select(AccountCondition.byAccountNo(accountNo.value()));
		return accountList.isEmpty() ? null : AccountModel.from(accountList.getFirst());
	}

	/**
	 * Accountテーブルで該当するレコードを取得する
	 *
	 * @param	accountId		アカウントId
	 * @return	AccountModel	{@link AccountModel}<p>
	 * 							取得できない場合はnullを返す
	 */
	@Override
	public AccountModel getByAccountId(AccountId accountId) {
		List<Account> accountList = accountMapper.select(AccountCondition.byAccountId(accountId.value()));
		return accountList.isEmpty() ? null : AccountModel.from(accountList.getFirst());
	}

	/**
	 * Accountテーブルへ登録する
	 *
	 * @param	accountModel		{@link AccountModel}
	 * @throws	GalleryException	登録に失敗した場合
	 */
	@Override
	public void regist(AccountModel accountModel) throws GalleryException {
		Account account = Account.from(accountModel, passwordEncoder);

		try {
			accountMapper.insert(account);
		}
		catch (DuplicateKeyException e) {
			log.warn("Account: Duplicate Key (AccountId: {})", accountModel.getAccountId().value(), e);
			throw ErrorEnum.FAIL_TO_REGIST_ACCOUNT.toException();
		}
	}

	/**
	 * Accountテーブルで該当するレコードを更新する
	 *
	 * @param	accountModel		{@link AccountModel}
	 * @throws	GalleryException	更新に失敗した場合
	 */
	@Override
	public void update(AccountModel accountModel) throws GalleryException {
		AccountCondition condition = AccountCondition.byAccountNo(accountModel.getAccountNo().value());
		AccountUpdateTarget target = AccountUpdateTarget.fromForUpdate(accountModel, passwordEncoder);

		if (accountMapper.update(condition, target) < 1) {
			log.warn("Account: Update Failed (AccountNo: {})", accountModel.getAccountNo().value());
			throw ErrorEnum.FAIL_TO_UPDATE_ACCOUNT.toException();
		}
	}

	/**
	 * Accountテーブルのログイン失敗回数を更新する
	 *
	 * @param	accountModel		{@link AccountModel}
	 * @throws	GalleryException	更新に失敗した場合
	 */
	@Override
	public void updateLoginFailureCount(AccountModel accountModel) throws GalleryException {
		AccountCondition condition = AccountCondition.byAccountNo(accountModel.getAccountNo().value());
		AccountUpdateTarget target = AccountUpdateTarget.fromForUpdateLoginFailure(accountModel);

		if (accountMapper.update(condition, target) < 1) {
			log.warn("Account: Update Failed (AccountNo: {})", accountModel.getAccountNo().value());
			throw ErrorEnum.FAIL_TO_UPDATE_ACCOUNT.toException();
		}
	}

	/**
	 * アカウントIDに該当するアカウントの存在有無をチェックする（新規登録用、除外なし）
	 *
	 * @param	accountId	アカウントID
	 * @return				true：存在する
	 */
	@Override
	public Boolean isExistAccount(AccountId accountId) {
		return accountMapper.isExistAccount(AccountCondition.forExistCheck(null, accountId.value()));
	}

	/**
	 * アカウントIDに該当するアカウントの存在有無をチェックする（更新用、自分自身を除外）
	 *
	 * @param	accountNo	検索対象外のアカウント番号
	 * @param	accountId	アカウントID
	 * @return				true：存在する
	 */
	@Override
	public Boolean isExistAccount(AccountNo accountNo, AccountId accountId) {
		return accountMapper.isExistAccount(AccountCondition.forExistCheck(accountNo.value(), accountId.value()));
	}

	/**
	 * アカウントの一覧を取得する
	 *
	 * @return	{@link AccountModelList}
	 */
	@Override
	public AccountModelList getAccountList() {
		List<Account> accountList = accountMapper.select(AccountCondition.forList());
		return AccountModelList.from(accountList);
	}

	/**
	 * 削除済みを含む全アカウントの一覧を取得する
	 *
	 * @return	{@link AccountModelList}
	 */
	@Override
	public AccountModelList getAccountListAll() {
		List<Account> accountList = accountMapper.select(AccountCondition.forAdminList());
		return AccountModelList.from(accountList);
	}

	/**
	 * Accountテーブルから該当するレコードを物理削除する
	 *
	 * @param	accountNo	アカウント番号
	 */
	@Override
	public void delete(AccountNo accountNo) {
		accountMapper.delete(AccountCondition.byAccountNo(accountNo.value()));
	}

	/**
	 * アカウントの行ロックを取得する（排他制御用）
	 *
	 * @param	accountNo	アカウント番号
	 */
	@Override
	public void lockForUpdate(AccountNo accountNo) {
		accountMapper.lockAccount(accountNo.value());
	}
}
