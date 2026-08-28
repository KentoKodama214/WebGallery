package com.web.gallery.model;

import java.time.Clock;
import java.time.OffsetDateTime;

import com.web.gallery.controller.request.AccountRegistRequest;
import com.web.gallery.controller.request.AccountUpdateRequest;
import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.account.BirthDate;
import com.web.gallery.domain.account.BirthplacePrefectureKbnCode;
import com.web.gallery.domain.account.FreeMemo;
import com.web.gallery.domain.account.LastLoginDatetime;
import com.web.gallery.domain.account.LoginFailureCount;
import com.web.gallery.domain.account.Password;
import com.web.gallery.domain.account.ResidentPrefectureKbnCode;
import com.web.gallery.domain.common.IsDeleted;
import com.web.gallery.entity.Account;
import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.enumeration.SexEnum;

import lombok.Builder;
import lombok.Value;

/**
 * アカウント情報を受け渡すためのModelクラス
 * <p>
 * {@code forUnlock}/{@code forLock}/{@code forLoginSuccess}のように
 * ログイン失敗回数等の部分更新専用のファクトリメソッドが存在し、全ファクトリメソッドに共通して
 * 必須となるプロパティが存在しないため、意図的に{@code @NonNull}を付与していない。
 */
@Value
@Builder
public class AccountModel {
	/** アカウント番号 */
	private AccountNo accountNo;

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

	/** 削除フラグ */
	private IsDeleted isDeleted;

	/**
	 * AccountエンティティからAccountModelを生成する
	 *
	 * @param	entity	{@link Account}
	 * @return			{@link AccountModel}
	 */
	public static AccountModel from(Account entity) {
		return AccountModel.builder()
				.accountNo(new AccountNo(entity.getAccountNo()))
				.accountId(new AccountId(entity.getAccountId()))
				.accountName(new AccountName(entity.getAccountName()))
				.password(new Password(entity.getPassword()))
				.birthdate(entity.getBirthdate() != null ? new BirthDate(entity.getBirthdate()) : null)
				.sexKbn(entity.getSexKbn())
				.birthplacePrefectureKbnCode(entity.getBirthplacePrefectureKbnCode() != null ? new BirthplacePrefectureKbnCode(entity.getBirthplacePrefectureKbnCode()) : null)
				.residentPrefectureKbnCode(entity.getResidentPrefectureKbnCode() != null ? new ResidentPrefectureKbnCode(entity.getResidentPrefectureKbnCode()) : null)
				.freeMemo(entity.getFreeMemo() != null ? new FreeMemo(entity.getFreeMemo()) : null)
				.authorityKbn(entity.getAuthorityKbn())
				.lastLoginDatetime(entity.getLastLoginDatetime() != null ? new LastLoginDatetime(entity.getLastLoginDatetime()) : null)
				.loginFailureCount(entity.getLoginFailureCount() != null ? new LoginFailureCount(entity.getLoginFailureCount()) : null)
				.isDeleted(new IsDeleted(entity.getIsDeleted()))
				.build();
	}

	/**
	 * アカウント登録リクエストからAccountModelを生成する
	 *
	 * @param	request	{@link AccountRegistRequest}
	 * @return			{@link AccountModel}
	 */
	public static AccountModel from(AccountRegistRequest request) {
		return AccountModel.builder()
				.accountId(new AccountId(request.getAccountId()))
				.accountName(new AccountName(request.getAccountName()))
				.password(new Password(request.getPassword()))
				.birthdate(request.getBirthdate() != null ? new BirthDate(request.getBirthdate()) : null)
				.sexKbn(request.getSexKbn())
				.birthplacePrefectureKbnCode(request.getBirthplacePrefectureKbnCode() != null ? new BirthplacePrefectureKbnCode(request.getBirthplacePrefectureKbnCode()) : null)
				.residentPrefectureKbnCode(request.getResidentPrefectureKbnCode() != null ? new ResidentPrefectureKbnCode(request.getResidentPrefectureKbnCode()) : null)
				.freeMemo(request.getFreeMemo() != null ? new FreeMemo(request.getFreeMemo()) : null)
				.loginFailureCount(new LoginFailureCount(0))
				.build();
	}

	/**
	 * アカウント更新リクエストからAccountModelを生成する
	 *
	 * @param	request		{@link AccountUpdateRequest}
	 * @param	accountNo	アカウント番号
	 * @return				{@link AccountModel}
	 */
	public static AccountModel from(AccountUpdateRequest request, Long accountNo) {
		return AccountModel.builder()
				.accountNo(new AccountNo(accountNo))
				.accountId(new AccountId(request.getAccountId()))
				.accountName(new AccountName(request.getAccountName()))
				.password(request.getNewPassword().isEmpty() ? null : new Password(request.getNewPassword()))
				.birthdate(request.getBirthdate() != null ? new BirthDate(request.getBirthdate()) : null)
				.sexKbn(request.getSexKbn())
				.birthplacePrefectureKbnCode(request.getBirthplacePrefectureKbnCode() != null ? new BirthplacePrefectureKbnCode(request.getBirthplacePrefectureKbnCode()) : null)
				.residentPrefectureKbnCode(request.getResidentPrefectureKbnCode() != null ? new ResidentPrefectureKbnCode(request.getResidentPrefectureKbnCode()) : null)
				.freeMemo(request.getFreeMemo() != null ? new FreeMemo(request.getFreeMemo()) : null)
				.build();
	}

	/**
	 * アカウントロック解除用のAccountModelを生成する（ログイン失敗回数を0にリセット）
	 *
	 * @param	accountNo	アカウント番号
	 * @return				{@link AccountModel}
	 */
	public static AccountModel forUnlock(Long accountNo) {
		return AccountModel.builder()
				.accountNo(new AccountNo(accountNo))
				.loginFailureCount(new LoginFailureCount(0))
				.build();
	}

	/**
	 * アカウント強制ロック用のAccountModelを生成する（ログイン失敗回数を上限値に設定）
	 *
	 * @param	accountNo	アカウント番号
	 * @param	failCount	ログイン失敗回数の上限値
	 * @return				{@link AccountModel}
	 */
	public static AccountModel forLock(Long accountNo, Integer failCount) {
		return AccountModel.builder()
				.accountNo(new AccountNo(accountNo))
				.loginFailureCount(new LoginFailureCount(failCount))
				.build();
	}

	/**
	 * 認証成功時のAccountModelを生成する（最終ログイン日時を現在時刻に設定し、ログイン失敗回数を0にリセット）
	 *
	 * @param	accountNo	アカウント番号
	 * @param	clock		現在時刻取得用の{@link Clock}
	 * @return				{@link AccountModel}
	 */
	public static AccountModel forLoginSuccess(AccountNo accountNo, Clock clock) {
		return AccountModel.builder()
				.accountNo(accountNo)
				.lastLoginDatetime(new LastLoginDatetime(OffsetDateTime.now(clock)))
				.loginFailureCount(new LoginFailureCount(0))
				.build();
	}
}
