package com.web.gallary.entity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.web.gallary.constant.Consts;
import com.web.gallary.enumuration.AuthorityEnum;
import com.web.gallary.enumuration.SexEnum;
import com.web.gallary.model.AccountModel;

import lombok.Builder;
import lombok.Data;

/**
 * アカウントテーブルのEntityクラス
 */
@Data
@Builder
public class Account {
	/** アカウント番号 */
	private Integer accountNo;

	/** 作成者 */
	private Integer createdBy;

	/** 作成日時 */
	private OffsetDateTime createdAt;

	/** 更新者 */
	private Integer updatedBy;

	/** 更新日時 */
	private OffsetDateTime updatedAt;

	/** 削除フラグ */
	private Boolean isDeleted;

	/** アカウントID */
	private String accountId;

	/** アカウント名 */
	private String accountName;

	/** パスワード */
	private String password;

	/** 生年月日 */
	private LocalDate birthdate;

	/** 
	 * 性別区分
	 * <p>
	 * {@link SexEnum}
	 */
	private SexEnum sexKbn;

	/** 出身都道府県区分コード */
	private String birthplacePrefectureKbnCode;

	/** 在住都道府県区分コード */
	private String residentPrefectureKbnCode;

	/** フリーメモ */
	private String freeMemo;

	/**
	 * 権限区分
	 * <p>
	 * {@link AuthorityEnum}
	 */
	private AuthorityEnum authorityKbn;

	/** 最終ログイン日時 */
	private OffsetDateTime lastLoginDatetime;
	
	/** ログイン失敗回数 */
	private Integer loginFailureCount;

	/**
	 * アカウント登録用のAccountModelからAccountエンティティを生成する
	 *
	 * @param	model			{@link AccountModel}
	 * @param	passwordEncoder	{@link PasswordEncoder}
	 * @return					{@link Account}
	 */
	public static Account from(AccountModel model, PasswordEncoder passwordEncoder) {
		return Account.builder()
				.createdBy(0)
				.updatedBy(0)
				.accountId(model.getAccountId())
				.accountName(model.getAccountName())
				.password(passwordEncoder.encode(model.getPassword()))
				.birthdate(
					Optional.ofNullable(model.getBirthdate()).orElse(Consts.MIN_LOCAL_DATE))
				.sexKbn(
					Optional.ofNullable(model.getSexKbn()).orElse(SexEnum.NONE))
				.birthplacePrefectureKbnCode(
					Optional.ofNullable(model.getBirthplacePrefectureKbnCode()).orElse(Consts.STRING_NONE))
				.residentPrefectureKbnCode(
					Optional.ofNullable(model.getResidentPrefectureKbnCode()).orElse(Consts.STRING_NONE))
				.freeMemo(
					Optional.ofNullable(model.getFreeMemo()).orElse(Consts.STRING_EMPTY))
				.authorityKbn(AuthorityEnum.MINI)
				.lastLoginDatetime(Consts.MIN_OFFSET_DATE_TIME)
				.loginFailureCount(0)
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
					Optional.ofNullable(model.getBirthdate()).orElse(Consts.MIN_LOCAL_DATE))
				.sexKbn(
					Optional.ofNullable(model.getSexKbn()).orElse(SexEnum.NONE))
				.birthplacePrefectureKbnCode(
					Optional.ofNullable(model.getBirthplacePrefectureKbnCode()).orElse(Consts.STRING_NONE))
				.residentPrefectureKbnCode(
					Optional.ofNullable(model.getResidentPrefectureKbnCode()).orElse(Consts.STRING_NONE))
				.freeMemo(
					Optional.ofNullable(model.getFreeMemo()).orElse(Consts.STRING_EMPTY))
				.lastLoginDatetime(
					Optional.ofNullable(model.getLastLoginDatetime()).orElse(Consts.MIN_OFFSET_DATE_TIME))
				.loginFailureCount(
					Optional.ofNullable(model.getLoginFailureCount()).orElse(0))
				.build();

		if (!Objects.isNull(model.getPassword())) {
			account.setPassword(passwordEncoder.encode(model.getPassword()));
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
				.loginFailureCount(Optional.ofNullable(model.getLoginFailureCount()).orElse(0))
				.build();
	}

	/**
	 * アカウント番号で検索するための条件用Accountエンティティを生成する
	 *
	 * @param	accountNo	アカウント番号
	 * @return				{@link Account}
	 */
	public static Account conditionByAccountNo(Integer accountNo) {
		return Account.builder()
				.accountNo(accountNo)
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
				.accountId(accountId)
				.build();
	}

	/**
	 * アカウント存在チェック用の条件Accountエンティティを生成する
	 *
	 * @param	accountNo	検索対象外のアカウント番号
	 * @param	accountId	アカウントID
	 * @return				{@link Account}
	 */
	public static Account conditionForExistCheck(Integer accountNo, String accountId) {
		return Account.builder()
				.accountNo(accountNo)
				.accountId(accountId)
				.build();
	}

	/**
	 * アカウント一覧取得用の条件Accountエンティティを生成する
	 *
	 * @return	{@link Account}
	 */
	public static Account conditionForList() {
		return Account.builder()
				.isDeleted(false)
				.build();
	}
}