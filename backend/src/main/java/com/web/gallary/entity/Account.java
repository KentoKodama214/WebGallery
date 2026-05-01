package com.web.gallary.entity;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.web.gallary.enumuration.AuthorityEnum;
import com.web.gallary.enumuration.SexEnum;

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
}