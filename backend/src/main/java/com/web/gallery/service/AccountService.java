package com.web.gallery.service;

import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.model.AccountModel;
import com.web.gallery.model.AccountModelList;

/**
 * アカウントに関するビジネスロジックを行うServiceクラス
 */
public interface AccountService {
	/**
	 * アカウントを新規登録する
	 *
	 * @param	accountModel		{@link AccountModel}
	 * @return						登録に成功した場合、true
	 * @throws	GalleryException	登録に失敗した場合
	 */
	Boolean registAccount(AccountModel accountModel) throws GalleryException;

	/**
	 * アカウントを更新する
	 *
	 * @param	accountModel		{@link AccountModel}
	 * @return						更新に成功した場合、true
	 * @throws	GalleryException	更新に失敗した場合
	 */
	Boolean updateAccount(AccountModel accountModel) throws GalleryException;

	/**
	 * アカウントIDからアカウント情報を取得する
	 *
	 * @param	accountId	アカウントID
	 * @return				{@link AccountModel}
	 */
	AccountModel getAccountById(AccountId accountId);

	/**
	 * アカウントの一覧を取得する
	 *
	 * @return	{@link AccountModelList}
	 */
	AccountModelList getAccountList();

	/**
	 * 管理者用：削除済みを含む全アカウントの一覧を取得する
	 *
	 * @return	{@link AccountModelList}
	 */
	AccountModelList getAccountListForAdmin();

	/**
	 * 管理者用：アカウントのロックを解除する（ログイン失敗回数を0にリセット）
	 *
	 * @param	accountNo			アカウント番号
	 * @throws	GalleryException	更新に失敗した場合
	 */
	void unlockAccount(AccountNo accountNo) throws GalleryException;

	/**
	 * 管理者用：アカウントを強制ロックする（ログイン失敗回数を上限超過に設定）
	 *
	 * @param	accountNo			アカウント番号
	 * @throws	GalleryException	更新に失敗した場合
	 */
	void lockAccount(AccountNo accountNo) throws GalleryException;

	/**
	 * アカウントを削除する
	 *
	 * @param	accountNo	アカウント番号
	 * @param	accountId	アカウントID
	 */
	void deleteAccount(AccountNo accountNo, AccountId accountId);
}
