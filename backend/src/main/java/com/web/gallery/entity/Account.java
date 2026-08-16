package com.web.gallery.entity;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.web.gallery.constant.Consts;
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
import com.web.gallery.domain.common.CreatedBy;
import com.web.gallery.domain.common.CreatedAt;
import com.web.gallery.domain.common.IsDeleted;
import com.web.gallery.domain.common.UpdatedBy;
import com.web.gallery.domain.common.UpdatedAt;
import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.enumeration.SexEnum;
import com.web.gallery.model.AccountModel;

import lombok.Builder;
import lombok.Data;

/**
 * アカウントテーブルのEntityクラス
 */
@Data
@Builder
public class Account {
	/** アカウント番号 */
	private AccountNo accountNo;

	/** 作成者 */
	private CreatedBy createdBy;

	/** 作成日時 */
	private CreatedAt createdAt;

	/** 更新者 */
	private UpdatedBy updatedBy;

	/** 更新日時 */
	private UpdatedAt updatedAt;

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
	 * アカウント登録用のAccountModelからAccountエンティティを生成する
	 *
	 * @param	model			{@link AccountModel}
	 * @param	passwordEncoder	{@link PasswordEncoder}
	 * @return					{@link Account}
	 */
	public static Account from(AccountModel model, PasswordEncoder passwordEncoder) {
		return Account.builder()
				.createdBy(new CreatedBy(0L))
				.updatedBy(new UpdatedBy(0L))
				.accountId(model.getAccountId())
				.accountName(model.getAccountName())
				.password(new Password(passwordEncoder.encode(model.getPassword().value())))
				.birthdate(
					Optional.ofNullable(model.getBirthdate())
						.orElse(new BirthDate(Consts.MIN_LOCAL_DATE)))
				.sexKbn(
					Optional.ofNullable(model.getSexKbn()).orElse(SexEnum.NONE))
				.birthplacePrefectureKbnCode(
					Optional.ofNullable(model.getBirthplacePrefectureKbnCode())
						.orElse(new BirthplacePrefectureKbnCode(Consts.STRING_NONE)))
				.residentPrefectureKbnCode(
					Optional.ofNullable(model.getResidentPrefectureKbnCode())
						.orElse(new ResidentPrefectureKbnCode(Consts.STRING_NONE)))
				.freeMemo(
					Optional.ofNullable(model.getFreeMemo())
						.orElse(new FreeMemo(Consts.STRING_EMPTY)))
				.authorityKbn(AuthorityEnum.MINI)
				.lastLoginDatetime(new LastLoginDatetime(Consts.MIN_OFFSET_DATE_TIME))
				.loginFailureCount(new LoginFailureCount(0))
				.build();
	}

	/**
	 * アカウント更新用のAccountModelからAccountエンティティを生成する
	 *
	 * @param	model			{@link AccountModel}
	 * @param	passwordEncoder	{@link PasswordEncoder}
	 * @return					{@link Account}
	 */
	public static Account fromForUpdate(AccountModel model, PasswordEncoder passwordEncoder) {
		Account account = Account.builder()
				.accountId(model.getAccountId())
				.accountName(model.getAccountName())
				.birthdate(
					Optional.ofNullable(model.getBirthdate())
						.orElse(new BirthDate(Consts.MIN_LOCAL_DATE)))
				.sexKbn(
					Optional.ofNullable(model.getSexKbn()).orElse(SexEnum.NONE))
				.birthplacePrefectureKbnCode(
					Optional.ofNullable(model.getBirthplacePrefectureKbnCode())
						.orElse(new BirthplacePrefectureKbnCode(Consts.STRING_NONE)))
				.residentPrefectureKbnCode(
					Optional.ofNullable(model.getResidentPrefectureKbnCode())
						.orElse(new ResidentPrefectureKbnCode(Consts.STRING_NONE)))
				.freeMemo(
					Optional.ofNullable(model.getFreeMemo())
						.orElse(new FreeMemo(Consts.STRING_EMPTY)))
				.lastLoginDatetime(
					Optional.ofNullable(model.getLastLoginDatetime())
						.orElse(new LastLoginDatetime(Consts.MIN_OFFSET_DATE_TIME)))
				.loginFailureCount(
					Optional.ofNullable(model.getLoginFailureCount())
						.orElse(new LoginFailureCount(0)))
				.build();

		if (model.getPassword() != null) {
			account.setPassword(new Password(passwordEncoder.encode(model.getPassword().value())));
		}

		return account;
	}

	/**
	 * ログイン失敗回数更新用のAccountModelからAccountエンティティを生成する
	 *
	 * @param	model	{@link AccountModel}
	 * @return			{@link Account}
	 */
	public static Account fromForUpdateLoginFailure(AccountModel model) {
		return Account.builder()
				.lastLoginDatetime(model.getLastLoginDatetime())
				.loginFailureCount(
					Optional.ofNullable(model.getLoginFailureCount())
						.orElse(new LoginFailureCount(0)))
				.build();
	}

	/**
	 * アカウント番号で検索するための条件用Accountエンティティを生成する
	 *
	 * @param	accountNo	アカウント番号
	 * @return				{@link Account}
	 */
	public static Account conditionByAccountNo(Long accountNo) {
		return Account.builder()
				.accountNo(new AccountNo(accountNo))
				.build();
	}

	/**
	 * アカウントIDで検索するための条件用Accountエンティティを生成する
	 *
	 * @param	accountId	アカウントID
	 * @return				{@link Account}
	 */
	public static Account conditionByAccountId(String accountId) {
		return Account.builder()
				.accountId(new AccountId(accountId))
				.build();
	}

	/**
	 * アカウント存在チェック用の条件Accountエンティティを生成する
	 *
	 * @param	accountNo	検索対象外のアカウント番号
	 * @param	accountId	アカウントID
	 * @return				{@link Account}
	 */
	public static Account conditionForExistCheck(Long accountNo, String accountId) {
		return Account.builder()
				.accountNo(accountNo != null ? new AccountNo(accountNo) : null)
				.accountId(new AccountId(accountId))
				.build();
	}

	/**
	 * アカウント一覧取得用の条件Accountエンティティを生成する
	 *
	 * @return	{@link Account}
	 */
	public static Account conditionForList() {
		return Account.builder()
				.isDeleted(new IsDeleted(false))
				.build();
	}

	/**
	 * 管理者用アカウント一覧取得用の条件Accountエンティティを生成する（全件取得）
	 *
	 * @return	{@link Account}
	 */
	public static Account conditionForAdminList() {
		return Account.builder().build();
	}
}
