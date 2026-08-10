package com.web.gallery.repository;

import java.util.List;

import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.model.AccountModel;

/**
 * アカウントデータを永続化するRepositoryクラス
 */
public interface AccountRepository {
	/**
	 * Accountテーブルで該当するレコードを取得する
	 *
	 * @param	accountNo		アカウント番号
	 * @return	AccountModel	{@link AccountModel}<p>
	 * 							取得できない場合はnullを返す
	 */
	AccountModel getByAccountNo(Long accountNo);

	/**
	 * Accountテーブルで該当するレコードを取得する
	 *
	 * @param	accountId		アカウントId
	 * @return	AccountModel	{@link AccountModel}<p>
	 * 							取得できない場合はnullを返す
	 */
	AccountModel getByAccountId(String accountId);

	/**
	 * Accountテーブルへ登録する
	 *
	 * @param	accountModel			{@link AccountModel}
	 * @throws	RegistFailureException	登録に失敗した場合
	 */
	void regist(AccountModel accountModel) throws RegistFailureException;

	/**
	 * Accountテーブルで該当するレコードを更新する
	 *
	 * @param	accountModel			{@link AccountModel}
	 * @throws	UpdateFailureException	更新に失敗した場合
	 */
	void update(AccountModel accountModel) throws UpdateFailureException;

	/**
	 * Accountテーブルのログイン失敗回数を更新する
	 *
	 * @param	accountModel			{@link AccountModel}
	 * @throws	UpdateFailureException	更新に失敗した場合
	 */
	void updateLoginFailureCount(AccountModel accountModel) throws UpdateFailureException;

	/**
	 * アカウントIDに該当するアカウントの存在有無をチェックする
	 *
	 * @param	accountNo	検索対象外のアカウント番号
	 * @param	accountId	アカウントID
	 * @return				true：存在する
	 */
	Boolean isExistAccount(Long accountNo, String accountId);

	/**
	 * アカウントの一覧を取得する
	 *
	 * @return	{@link AccountModel}
	 */
	List<AccountModel> getAccountList();

	/**
	 * 削除済みを含む全アカウントの一覧を取得する
	 *
	 * @return	{@link AccountModel}
	 */
	List<AccountModel> getAccountListAll();

	/**
	 * Accountテーブルから該当するレコードを物理削除する
	 *
	 * @param	accountNo	アカウント番号
	 */
	void delete(Long accountNo);
}
