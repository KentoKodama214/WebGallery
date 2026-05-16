package com.web.gallary.repository.impl;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import com.web.gallary.entity.Account;
import com.web.gallary.enumuration.ErrorEnum;
import com.web.gallary.exception.RegistFailureException;
import com.web.gallary.exception.UpdateFailureException;
import com.web.gallary.mapper.AccountMapper;
import com.web.gallary.model.AccountModel;
import com.web.gallary.repository.AccountRepository;

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
	public AccountModel getByAccountNo(Integer accountNo) {
		Account account = Account.builder()
				.accountNo(accountNo)
				.build();

		List<Account> accountList = accountMapper.select(account);

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
	public AccountModel getByAccountId(String accountId) {
		Account account = Account.builder()
				.accountId(accountId)
				.build();

		List<Account> accountList = accountMapper.select(account);

		return accountList.isEmpty() ? null : AccountModel.from(accountList.getFirst());
	}

	/**
	 * Accountテーブルへ登録する
	 * 
	 * @param	accountModel			{@link AccountModel}
	 * @throws	RegistFailureException	登録に失敗した場合
	 */
	@Override
	public void regist(AccountModel accountModel) throws RegistFailureException {
		Account account = Account.from(accountModel, passwordEncoder);
		
		try {
			accountMapper.insert(account);
		}
		catch (DuplicateKeyException e) {
			log.warn("Account: Duplicate Key (AccountId: {})", accountModel.getAccountId(), e);
			throw new RegistFailureException(ErrorEnum.FAIL_TO_REGIST_ACCOUNT);
		}
	}

	/**
	 * Accountテーブルで該当するレコードを更新する
	 * 
	 * @param	accountModel			{@link AccountModel}
	 * @throws	UpdateFailureException	更新に失敗した場合 
	 */
	@Override
	public void update(AccountModel accountModel) throws UpdateFailureException {
		Account cndAccount = Account.builder().accountNo(accountModel.getAccountNo()).build();
		Account targetAccount = Account.fromForUpdate(accountModel, passwordEncoder);

		if (accountMapper.update(cndAccount, targetAccount) < 1) {
			log.warn("Account: Update Failed (AccountNo: {})", accountModel.getAccountNo());
			throw new UpdateFailureException(ErrorEnum.FAIL_TO_UPDATE_ACCOUNT);
		}
	}
	
	/**
	 * Accountテーブルのログイン失敗回数を更新する
	 * 
	 * @param	accountModel			{@link AccountModel}
	 * @throws	UpdateFailureException	更新に失敗した場合
	 */
	@Override
	public void updateLoginFailureCount(AccountModel accountModel) throws UpdateFailureException {
		Account cndAccount = Account.builder().accountNo(accountModel.getAccountNo()).build();
		Account targetAccount = Account.fromForUpdateLoginFailure(accountModel);

		if (accountMapper.update(cndAccount, targetAccount) < 1) {
			log.warn("Account: Update Failed (AccountNo: {})", accountModel.getAccountNo());
			throw new UpdateFailureException(ErrorEnum.FAIL_TO_UPDATE_ACCOUNT);
		}
	}
	
	/**
	 * アカウントIDに該当するアカウントの存在有無をチェックする
	 * 
	 * @param	accountNo	検索対象外のアカウント番号
	 * @param	accountId	アカウントID
	 * @return				true：存在する
	 */
	@Override
	public Boolean isExistAccount(Integer accountNo, String accountId) {
		Account account =  Account.builder().accountNo(accountNo).accountId(accountId).build();
		return accountMapper.isExistAccount(account);
	}
	
	/**
	 * アカウントの一覧を取得する
	 *
	 * @return	{@link AccountModel}
	 */
	@Override
	public List<AccountModel> getAccountList() {
		Account account = Account.builder().isDeleted(false).build();

		List<Account> accountList = accountMapper.select(account);

		return accountList.stream().map(AccountModel::from).toList();
	}
}