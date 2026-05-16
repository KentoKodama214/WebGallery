package com.web.gallary.controller.response;

import java.time.LocalDate;

import com.web.gallary.constant.Consts;
import com.web.gallary.entity.Account;
import com.web.gallary.enumuration.SexEnum;

import lombok.Builder;
import lombok.Data;

/**
 * アカウント詳細情報のレスポンスパラメータを保持するクラス
 */
@Data
@Builder
public class AccountDetailResponse {
	/** アカウントID */
	private String accountId;

	/** アカウント名 */
	private String accountName;

	/** 生年月日 */
	private LocalDate birthdate;

	/** 性別区分 */
	private SexEnum sexKbn;

	/** 出身地都道府県区分コード */
	private String birthplacePrefectureKbnCode;

	/** 居住地都道府県区分コード */
	private String residentPrefectureKbnCode;

	/** フリーメモ */
	private String freeMemo;

	/**
	 * AccountエンティティからAccountDetailResponseを生成する
	 *
	 * @param	account	{@link Account}
	 * @return			{@link AccountDetailResponse}
	 */
	public static AccountDetailResponse from(Account account) {
		return AccountDetailResponse.builder()
				.accountId(account.getAccountId())
				.accountName(account.getAccountName())
				.birthdate(Consts.MIN_LOCAL_DATE.equals(account.getBirthdate()) ? null : account.getBirthdate())
				.sexKbn(account.getSexKbn())
				.birthplacePrefectureKbnCode(account.getBirthplacePrefectureKbnCode())
				.residentPrefectureKbnCode(account.getResidentPrefectureKbnCode())
				.freeMemo(account.getFreeMemo())
				.build();
	}
}
