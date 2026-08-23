package com.web.gallery.entity;

import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.BirthDate;
import com.web.gallery.domain.account.BirthplacePrefectureKbnCode;
import com.web.gallery.domain.account.FreeMemo;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.account.LastLoginDatetime;
import com.web.gallery.domain.account.LoginFailureCount;
import com.web.gallery.domain.account.Password;
import com.web.gallery.domain.account.ResidentPrefectureKbnCode;
import com.web.gallery.domain.common.IsDeleted;
import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.enumeration.SexEnum;

import lombok.Builder;
import lombok.Data;

/**
 * アカウントテーブルの抽出条件クラス
 */
@Data
@Builder
public class AccountCondition {
	/** アカウント番号 */
	private AccountNo accountNo;

	/** 削除フラグ */
	private IsDeleted isDeleted;

	/** アカウントID */
	private AccountId accountId;

	/** アカウント名 */
	private AccountName accountName;

	/** パスワード */
	private Password password;

	/** 生年月日 */
	private BirthDate birthdate;

	/**
	 * 性別区分
	 * <p>
	 * {@link SexEnum}
	 */
	private SexEnum sexKbn;

	/** 出身都道府県区分コード */
	private BirthplacePrefectureKbnCode birthplacePrefectureKbnCode;

	/** 在住都道府県区分コード */
	private ResidentPrefectureKbnCode residentPrefectureKbnCode;

	/** フリーメモ */
	private FreeMemo freeMemo;

	/**
	 * 権限区分
	 * <p>
	 * {@link AuthorityEnum}
	 */
	private AuthorityEnum authorityKbn;

	/** 最終ログイン日時 */
	private LastLoginDatetime lastLoginDatetime;

	/** ログイン失敗回数 */
	private LoginFailureCount loginFailureCount;

	/**
	 * アカウント番号で検索するための抽出条件を生成する
	 *
	 * @param	accountNo	アカウント番号
	 * @return				{@link AccountCondition}
	 */
	public static AccountCondition byAccountNo(Long accountNo) {
		return AccountCondition.builder()
				.accountNo(new AccountNo(accountNo))
				.build();
	}

	/**
	 * アカウントIDで検索するための抽出条件を生成する
	 *
	 * @param	accountId	アカウントID
	 * @return				{@link AccountCondition}
	 */
	public static AccountCondition byAccountId(String accountId) {
		return AccountCondition.builder()
				.accountId(new AccountId(accountId))
				.build();
	}

	/**
	 * アカウント存在チェック用の抽出条件を生成する
	 *
	 * @param	accountNo	検索対象外のアカウント番号
	 * @param	accountId	アカウントID
	 * @return				{@link AccountCondition}
	 */
	public static AccountCondition forExistCheck(Long accountNo, String accountId) {
		return AccountCondition.builder()
				.accountNo(accountNo != null ? new AccountNo(accountNo) : null)
				.accountId(new AccountId(accountId))
				.build();
	}

	/**
	 * アカウント一覧取得用の抽出条件を生成する
	 *
	 * @return	{@link AccountCondition}
	 */
	public static AccountCondition forList() {
		return AccountCondition.builder()
				.isDeleted(new IsDeleted(false))
				.build();
	}

	/**
	 * 管理者用アカウント一覧取得用の抽出条件を生成する（全件取得）
	 *
	 * @return	{@link AccountCondition}
	 */
	public static AccountCondition forAdminList() {
		return AccountCondition.builder().build();
	}
}
