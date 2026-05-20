package com.web.gallery.controller.response;

import java.time.LocalDate;

import com.web.gallery.constant.Consts;
import com.web.gallery.enumuration.SexEnum;
import com.web.gallery.model.AccountModel;

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
	 * AccountModelからAccountDetailResponseを生成する
	 *
	 * @param	model	{@link AccountModel}
	 * @return			{@link AccountDetailResponse}
	 */
	public static AccountDetailResponse from(AccountModel model) {
		return AccountDetailResponse.builder()
				.accountId(model.getAccountId())
				.accountName(model.getAccountName())
				.birthdate(Consts.MIN_LOCAL_DATE.equals(model.getBirthdate()) ? null : model.getBirthdate())
				.sexKbn(model.getSexKbn())
				.birthplacePrefectureKbnCode(model.getBirthplacePrefectureKbnCode())
				.residentPrefectureKbnCode(model.getResidentPrefectureKbnCode())
				.freeMemo(model.getFreeMemo())
				.build();
	}
}
