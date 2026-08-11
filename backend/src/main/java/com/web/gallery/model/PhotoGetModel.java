package com.web.gallery.model;

import com.web.gallery.domain.account.AccountNo;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * 写真の情報を取得する時の情報を受け渡すためのModelクラス
 */
@Value
@Builder
public class PhotoGetModel {
	/** ログイン中のアカウントNo */
	private AccountNo accountNo;

	/** 写真のアカウントNo */
	@NonNull
	private AccountNo photoAccountNo;
}
