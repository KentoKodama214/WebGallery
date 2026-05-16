package com.web.gallary.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.web.gallary.controller.request.AccountRegistRequest;
import com.web.gallary.controller.request.AccountUpdateRequest;
import com.web.gallary.enumuration.AuthorityEnum;
import com.web.gallary.enumuration.SexEnum;

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