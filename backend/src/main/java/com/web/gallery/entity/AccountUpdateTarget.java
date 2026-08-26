package com.web.gallery.entity;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.web.gallery.constant.Consts;
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
	private Long updatedBy;

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
	 * アカウント更新用のAccountModelから更新対象を生成する
	 *
	 * @param	model			{@link AccountModel}
	 * @param	passwordEncoder	{@link PasswordEncoder}
	 * @return					{@link AccountUpdateTarget}
	 */
	public static AccountUpdateTarget fromForUpdate(AccountModel model, PasswordEncoder passwordEncoder) {
		AccountUpdateTarget target = AccountUpdateTarget.builder()
				.accountId(model.getAccountId() != null ? model.getAccountId().value() : null)
				.accountName(model.getAccountName() != null ? model.getAccountName().value() : null)
				.birthdate(model.getBirthdate() != null ? model.getBirthdate().value() : Consts.MIN_LOCAL_DATE)
				.sexKbn(SexEnum.getOrDefault(model.getSexKbn()))
				.birthplacePrefectureKbnCode(model.getBirthplacePrefectureKbnCode() != null ? model.getBirthplacePrefectureKbnCode().value() : Consts.STRING_NONE)
				.residentPrefectureKbnCode(model.getResidentPrefectureKbnCode() != null ? model.getResidentPrefectureKbnCode().value() : Consts.STRING_NONE)
				.freeMemo(model.getFreeMemo() != null ? model.getFreeMemo().value() : Consts.STRING_EMPTY)
				.lastLoginDatetime(model.getLastLoginDatetime() != null ? model.getLastLoginDatetime().value() : Consts.MIN_OFFSET_DATE_TIME)
				.loginFailureCount(model.getLoginFailureCount() != null ? model.getLoginFailureCount().value() : 0)
				.build();

		if (model.getPassword() != null) {
			target.setPassword(passwordEncoder.encode(model.getPassword().value()));
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
				.lastLoginDatetime(model.getLastLoginDatetime() != null ? model.getLastLoginDatetime().value() : null)
				.loginFailureCount(model.getLoginFailureCount() != null ? model.getLoginFailureCount().value() : 0)
				.build();
	}
}
