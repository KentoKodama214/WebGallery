package com.web.gallary.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.web.gallary.entity.Account;

/**
 * アカウントテーブルのMapperクラス
 */
@Mapper
public interface AccountMapper {
	/**
	 * 条件に該当するアカウントの一覧を取得する
	 * 
	 * @param	account	抽出条件
	 * @return			{@link Account}
	 */
	public List<Account> select(Account account);
	
	/**
	 * 条件に該当するアカウントの件数を取得する
	 * 
	 * @param	account	カウント条件
	 * @return			抽出件数
	 */
	public Integer count(Account account);

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
	 * @param	conditionAccount	更新対象の抽出条件
	 * @param	targetAccount		更新内容
	 * @return						更新件数
	 */
	public Integer update(@Param("condition") Account conditionAccount, @Param("target") Account targetAccount);
	
	/**
	 * アカウントを削除する
	 * 
	 * @param	account	削除対象の抽出条件
	 * @return			削除件数
	 */
	public Integer delete(Account account);
	
	/**
	 * アカウントIDに該当するアカウントが存在するかをチェックする
	 * 
	 * @param	account	{@link Account}
	 * @return			アカウントの存在有無
	 */
	public Boolean isExistAccount(Account account);
}