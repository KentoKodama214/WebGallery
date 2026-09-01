package com.web.gallery.entity;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.web.gallery.constant.Consts;
import com.web.gallery.model.AccountModel;
import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.enumeration.SexEnum;

import lombok.Builder;
import lombok.Data;

/**
 * アカウントテーブルのEntityクラス
 */
@Data
@Builder
public class Account {
	/** アカウント番号 */
	private Long accountNo;

	/** 作成者 */
	private Long createdBy;

	/** 作成日時 */
	private OffsetDateTime createdAt;

	/** 更新者 */
	private Long updatedBy;

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

	/** 管理者ロックフラグ */
	private Boolean isAdminLocked;

	/**
	 * アカウント登録用のAccountModelからAccountエンティティを生成する
	 *
	 * @param	model			{@link AccountModel}
	 * @param	passwordEncoder	{@link PasswordEncoder}
	 * @return					{@link Account}
	 */
	public static Account from(AccountModel model, PasswordEncoder passwordEncoder) {
		return Account.builder()
				.createdBy(0L)
				.updatedBy(0L)
				.accountId(model.getAccountId().value())
				.accountName(model.getAccountName().value())
				.password(passwordEncoder.encode(model.getPassword().value()))
				.birthdate(model.getBirthdate() != null ? model.getBirthdate().value() : Consts.MIN_LOCAL_DATE)
				.sexKbn(SexEnum.getOrDefault(model.getSexKbn()))
				.birthplacePrefectureKbnCode(model.getBirthplacePrefectureKbnCode() != null ? model.getBirthplacePrefectureKbnCode().value() : Consts.STRING_NONE)
				.residentPrefectureKbnCode(model.getResidentPrefectureKbnCode() != null ? model.getResidentPrefectureKbnCode().value() : Consts.STRING_NONE)
				.freeMemo(model.getFreeMemo() != null ? model.getFreeMemo().value() : Consts.STRING_EMPTY)
				.authorityKbn(AuthorityEnum.MINI)
				.lastLoginDatetime(Consts.MIN_OFFSET_DATE_TIME)
				.loginFailureCount(0)
				.isAdminLocked(false)
				.build();
	}
}
