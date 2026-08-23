package com.web.gallery.entity;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.web.gallery.domain.account.AccountId;
import com.web.gallery.domain.account.AccountName;
import com.web.gallery.domain.account.BirthDate;
import com.web.gallery.domain.account.BirthplacePrefectureKbnCode;
import com.web.gallery.domain.account.FreeMemo;
import com.web.gallery.domain.account.LastLoginDatetime;
import com.web.gallery.domain.account.LoginFailureCount;
import com.web.gallery.domain.account.Password;
import com.web.gallery.domain.account.ResidentPrefectureKbnCode;
import com.web.gallery.domain.common.IsDeleted;
import com.web.gallery.domain.common.UpdatedBy;
import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.enumeration.SexEnum;
import com.web.gallery.model.AccountModel;

import lombok.Builder;
import lombok.Data;

/**
 * アカウントテーブルの更新対象クラス
 */
@Data
@Builder
public class AccountUpdateTarget {
	/** 更新者 */
	private UpdatedBy updatedBy;

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
	 * アカウント更新用のAccountModelから更新対象を生成する
	 *
	 * @param	model			{@link AccountModel}
	 * @param	passwordEncoder	{@link PasswordEncoder}
	 * @return					{@link AccountUpdateTarget}
	 */
	public static AccountUpdateTarget fromForUpdate(AccountModel model, PasswordEncoder passwordEncoder) {
		AccountUpdateTarget target = AccountUpdateTarget.builder()
				.accountId(model.getAccountId())
				.accountName(model.getAccountName())
				.birthdate(BirthDate.getOrDefault(model.getBirthdate()))
				.sexKbn(SexEnum.getOrDefault(model.getSexKbn()))
				.birthplacePrefectureKbnCode(BirthplacePrefectureKbnCode.getOrDefault(model.getBirthplacePrefectureKbnCode()))
				.residentPrefectureKbnCode(ResidentPrefectureKbnCode.getOrDefault(model.getResidentPrefectureKbnCode()))
				.freeMemo(FreeMemo.getOrDefault(model.getFreeMemo()))
				.lastLoginDatetime(LastLoginDatetime.getOrDefault(model.getLastLoginDatetime()))
				.loginFailureCount(LoginFailureCount.getOrDefault(model.getLoginFailureCount()))
				.build();

		if (model.getPassword() != null) {
			target.setPassword(new Password(passwordEncoder.encode(model.getPassword().value())));
		}

		return target;
	}

	/**
	 * ログイン失敗回数更新用のAccountModelから更新対象を生成する
	 *
	 * @param	model	{@link AccountModel}
	 * @return			{@link AccountUpdateTarget}
	 */
	public static AccountUpdateTarget fromForUpdateLoginFailure(AccountModel model) {
		return AccountUpdateTarget.builder()
				.lastLoginDatetime(model.getLastLoginDatetime())
				.loginFailureCount(LoginFailureCount.getOrDefault(model.getLoginFailureCount()))
				.build();
	}
}
