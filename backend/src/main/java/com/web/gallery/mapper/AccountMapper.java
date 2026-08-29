package com.web.gallery.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.web.gallery.entity.Account;
import com.web.gallery.entity.AccountCondition;
import com.web.gallery.entity.AccountUpdateTarget;

/**
 * アカウントテーブルのMapperクラス
 */
@Mapper
public interface AccountMapper {
	/**
	 * 条件に該当するアカウントの一覧を取得する
	 *
	 * @param	condition	抽出条件
	 * @return				{@link Account}
	 */
	public List<Account> select(AccountCondition condition);

	/**
	 * 条件に該当するアカウントの件数を取得する
	 *
	 * @param	condition	カウント条件
	 * @return				抽出件数
	 */
	public Integer count(AccountCondition condition);

	/**
	 * アカウントを登録する
	 *
	 * @param	account	{@link Account}
	 * @return			登録件数
	 */
	public Integer insert(Account account);

	/**
	 * アカウントを更新する
	 *
	 * @param	condition	更新対象の抽出条件
	 * @param	target		更新内容
	 * @return				更新件数
	 */
	public Integer update(@Param("condition") AccountCondition condition, @Param("target") AccountUpdateTarget target);

	/**
	 * アカウントのログイン失敗回数をSQL側で原子的にインクリメントする
	 *
	 * @param	accountNo	アカウント番号
	 * @return				更新件数
	 */
	public Integer incrementLoginFailureCount(@Param("accountNo") Long accountNo);

	/**
	 * アカウントを削除する
	 *
	 * @param	condition	削除対象の抽出条件
	 * @return				削除件数
	 */
	public Integer delete(AccountCondition condition);

	/**
	 * アカウントIDに該当するアカウントが存在するかをチェックする
	 *
	 * @param	condition	{@link AccountCondition}
	 * @return				アカウントの存在有無
	 */
	public Boolean isExistAccount(AccountCondition condition);

	/**
	 * アカウントの行ロックを取得する（排他制御用）
	 *
	 * @param	accountNo	アカウント番号
	 * @return				ロックしたアカウント番号（存在しない場合はnull）
	 */
	public Long lockAccount(@Param("accountNo") Long accountNo);
}