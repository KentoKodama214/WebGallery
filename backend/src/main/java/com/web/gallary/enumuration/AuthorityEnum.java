package com.web.gallary.enumuration;

import java.util.Arrays;

import com.web.gallary.constant.Consts;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * アカウントの権限を管理するEnumクラス
 */
@Getter
@AllArgsConstructor
public enum AuthorityEnum {
	/** 
	 * 簡易ユーザー<p>
	 * サイトを閲覧したり、サービスを利用する人<p>
	 * 写真登録の上限は10枚（上限50MB）
	 */
	MINI(Consts.AUTHORITY_MINI),
	
	/** 
	 * 一般ユーザー<p>
	 * サイトを閲覧したり、サービスを利用する人<p>
	 * 写真登録の上限は1000枚（上限5GB）
	 */
	NORMAL(Consts.AUTHORITY_NORMAL),
	
	/** 
	 * 特別ユーザー<p>
	 * サイトを閲覧したり、サービスを利用する人<p>
	 * 写真登録の上限は無制限
	 */
	SPECIAL(Consts.AUTHORITY_SPECIAL),
	
	/** 
	 * 管理者<p>
	 * サイトを管理・運営する人<p>
	 * 写真登録の上限は無制限
	 */
	ADMINISTRATOR(Consts.ADMINISTRATOR);
	
	/** DBに保持する値 */
	private final String dbValue;
	
	/**
	 * 名称からEnumを取得する<p>
	 * 該当するEnumがなければ、デフォルトでMINIを返す
	 * 
	 * @param value 値
	 * @return {@link AuthorityEnum}
	 */
	public static AuthorityEnum getOrDefault(String value) {
		return Arrays.stream(AuthorityEnum.values())
				.filter(e -> e.getDbValue().equals(value))
				.findFirst()
				.orElse(AuthorityEnum.MINI);
    }
}