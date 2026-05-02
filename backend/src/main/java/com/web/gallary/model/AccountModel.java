package com.web.gallary.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;

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
}