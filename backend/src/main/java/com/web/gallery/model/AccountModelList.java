package com.web.gallery.model;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.web.gallery.entity.Account;

/**
 * AccountModelのコレクションを表すクラス
 */
public record AccountModelList(List<AccountModel> accountModelList) implements Iterable<AccountModel> {

	public AccountModelList {
		Objects.requireNonNull(accountModelList);
	}

	/**
	 * AccountModelのリストからAccountModelListを生成する
	 *
	 * @param	accountModelList	{@link AccountModel}のリスト
	 * @return						{@link AccountModelList}
	 */
	public static AccountModelList of(List<AccountModel> accountModelList) {
		return new AccountModelList(accountModelList);
	}

	/**
	 * 空のAccountModelListを生成する
	 *
	 * @return	{@link AccountModelList}
	 */
	public static AccountModelList empty() {
		return AccountModelList.of(List.of());
	}

	/**
	 * AccountエンティティのリストからAccountModelListを生成する
	 *
	 * @param	accountList	{@link Account}のリスト
	 * @return				{@link AccountModelList}
	 */
	public static AccountModelList from(List<Account> accountList) {
		return AccountModelList.of(accountList.stream().map(AccountModel::from).toList());
	}

	/**
	 * アカウントIDの昇順でソートしたAccountModelListを生成する
	 *
	 * @return	{@link AccountModelList}
	 */
	public AccountModelList sortByAccountId() {
		return AccountModelList.of(accountModelList.stream()
				.sorted(Comparator.comparing(accountModel -> accountModel.getAccountId().value()))
				.toList());
	}

	/**
	 * 削除フラグでフィルタリングしたAccountModelListを生成する
	 *
	 * @param	isDeleted	フィルター条件の削除フラグ
	 * @return				{@link AccountModelList}
	 */
	public AccountModelList filterByIsDeleted(Boolean isDeleted) {
		return AccountModelList.of(accountModelList.stream()
				.filter(accountModel -> accountModel.getIsDeleted().value().equals(isDeleted))
				.toList());
	}

	/**
	 * 要素数を取得する
	 *
	 * @return	要素数
	 */
	public Integer size() {
		return accountModelList.size();
	}

	/**
	 * 要素が空かどうかを取得する
	 *
	 * @return	要素が空の場合はtrue
	 */
	public Boolean isEmpty() {
		return accountModelList.isEmpty();
	}

	/**
	 * 指定インデックスの要素を取得する
	 *
	 * @param	index	インデックス
	 * @return			{@link AccountModel}
	 */
	public AccountModel get(int index) {
		return accountModelList.get(index);
	}

	/**
	 * Streamに変換する
	 *
	 * @return	{@link AccountModel}のStream
	 */
	public Stream<AccountModel> stream() {
		return accountModelList.stream();
	}

	/**
	 * Listに変換する
	 *
	 * @return	{@link AccountModel}のList
	 */
	public List<AccountModel> toList() {
		return accountModelList;
	}

	@Override
	public Iterator<AccountModel> iterator() {
		return accountModelList.iterator();
	}
}
