package com.web.gallery.repository;

import com.web.gallery.aggregate.Account;

/**
 * アカウント集約（{@link Account}）を永続化するRepositoryクラス
 */
public interface AccountAggregateRepository {
	/**
	 * アカウント集約を削除する<p>
	 * お気に入り・写真タグ・写真マスタ・リフレッシュトークン・アカウント本体を、
	 * ユースケース単位で整合性のある1操作として削除する
	 *
	 * @param	account	{@link Account}
	 */
	void delete(Account account);
}
