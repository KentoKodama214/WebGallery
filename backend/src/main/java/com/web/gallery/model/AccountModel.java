package com.web.gallery.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.web.gallery.controller.request.AccountRegistRequest;
import com.web.gallery.controller.request.AccountUpdateRequest;
import com.web.gallery.entity.Account;
import com.web.gallery.enumuration.AuthorityEnum;
import com.web.gallery.enumuration.SexEnum;

import lombok.Builder;
import lombok.Value;

/**
 * アカウント情報を受け渡すためのModelクラス
 */
@Value
@Builder
public class AccountModel {
	/** アカウント番号 */
	private Integer accountNo;
	
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

	/** 削除フラグ */
	private Boolean isDeleted;

	/**
	 * AccountエンティティからAccountModelを生成する
	 *
	 * @param	entity	{@link Account}
	 * @return			{@link AccountModel}
	 */
	public static AccountModel from(Account entity) {
		return AccountModel.builder()
				.accountNo(entity.getAccountNo())
				.accountId(entity.getAccountId())
				.accountName(entity.getAccountName())
				.password(entity.getPassword())
				.birthdate(entity.getBirthdate())
				.sexKbn(entity.getSexKbn())
				.birthplacePrefectureKbnCode(entity.getBirthplacePrefectureKbnCode())
				.residentPrefectureKbnCode(entity.getResidentPrefectureKbnCode())
				.freeMemo(entity.getFreeMemo())
				.authorityKbn(entity.getAuthorityKbn())
				.lastLoginDatetime(entity.getLastLoginDatetime())
				.loginFailureCount(entity.getLoginFailureCount())
				.isDeleted(entity.getIsDeleted())
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
				.accountId(request.getAccountId())
				.accountName(request.getAccountName())
				.password(request.getPassword())
				.birthdate(request.getBirthdate())
				.sexKbn(request.getSexKbn())
				.birthplacePrefectureKbnCode(request.getBirthplacePrefectureKbnCode())
				.residentPrefectureKbnCode(request.getResidentPrefectureKbnCode())
				.freeMemo(request.getFreeMemo())
				.loginFailureCount(0)
				.build();
	}

	/**
	 * アカウント更新リクエストからAccountModelを生成する
	 *
	 * @param	request		{@link AccountUpdateRequest}
	 * @param	accountNo	アカウント番号
	 * @return				{@link AccountModel}
	 */
	public static AccountModel from(AccountUpdateRequest request, Integer accountNo) {
		return AccountModel.builder()
				.accountNo(accountNo)
				.accountId(request.getAccountId())
				.accountName(request.getAccountName())
				.password(request.getNewPassword().isEmpty() ? null : request.getNewPassword())
				.birthdate(request.getBirthdate())
				.sexKbn(request.getSexKbn())
				.birthplacePrefectureKbnCode(request.getBirthplacePrefectureKbnCode())
				.residentPrefectureKbnCode(request.getResidentPrefectureKbnCode())
				.freeMemo(request.getFreeMemo())
				.build();
	}
}