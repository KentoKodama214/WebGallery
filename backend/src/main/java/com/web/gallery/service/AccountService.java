package com.web.gallery.service;

import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.account.Password;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.model.AccountListGetModel;
import com.web.gallery.model.AccountModel;
import com.web.gallery.model.AccountPageModel;

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
	 * アカウントを更新する<p>
	 * パスワードを変更する場合（{@code accountModel.getPassword()}が非null）は、
	 * {@code currentPassword}による本人確認（再認証）を行う
	 *
	 * @param	accountModel		{@link AccountModel}
	 * @param	currentPassword		現在のパスワード（パスワード変更時のみ必須、それ以外はnull可）
	 * @return						アカウントIDが重複しており更新をスキップした場合、true
	 * @throws	GalleryException	更新に失敗した場合、または現在のパスワードが一致しない場合
	 */
	Boolean updateAccount(AccountModel accountModel, Password currentPassword) throws GalleryException;

	/**
	 * アカウントIDからアカウント情報を取得する
	 *
	 * @param	accountId	アカウントID
	 * @return				{@link AccountModel}
	 */
	AccountModel getAccountById(AccountId accountId);

	/**
	 * アカウントの一覧を、ページング情報に従い取得する
	 *
	 * @param	accountListGetModel	{@link AccountListGetModel}
	 * @return						{@link AccountPageModel}
	 */
	AccountPageModel getAccountList(AccountListGetModel accountListGetModel);

	/**
	 * 管理者用：削除済みを含む全アカウントの一覧を、ページング情報に従い取得する
	 *
	 * @param	accountListGetModel	{@link AccountListGetModel}
	 * @return						{@link AccountPageModel}
	 */
	AccountPageModel getAccountListForAdmin(AccountListGetModel accountListGetModel);

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
	 * アカウントを削除する<p>
	 * {@code currentPassword}による本人確認（再認証）を行う
	 *
	 * @param	accountNo			アカウント番号
	 * @param	accountId			アカウントID
	 * @param	currentPassword		現在のパスワード
	 * @throws	GalleryException	現在のパスワードが一致しない場合
	 */
	void deleteAccount(AccountNo accountNo, AccountId accountId, Password currentPassword) throws GalleryException;
}
