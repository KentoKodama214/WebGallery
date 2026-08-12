package com.web.gallery.dto;

import com.web.gallery.domain.account.AccountNo;

import lombok.Data;

/**
 * 写真の一覧を取得するパラメータDtoクラス
 */
@Data
public class PhotoListGetDto {
	/** ログイン中のアカウントNo */
	private AccountNo accountNo;

	/** 写真のアカウントNo */
	private AccountNo photoAccountNo;
}
