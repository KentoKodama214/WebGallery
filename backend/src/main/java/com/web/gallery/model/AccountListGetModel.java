package com.web.gallery.model;

import com.web.gallery.controller.request.AccountListRequest;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * アカウントの一覧を取得するために必要な情報を受け渡すためのModelクラス
 */
@Value
@Builder
public class AccountListGetModel {
	/** ページ番号 */
	@NonNull
	private Integer pageNo;

	/**
	 * アカウント一覧リクエストからAccountListGetModelを生成する
	 *
	 * @param	request	{@link AccountListRequest}
	 * @return			{@link AccountListGetModel}
	 */
	public static AccountListGetModel from(AccountListRequest request) {
		return AccountListGetModel.builder()
				.pageNo(request.getPageNo())
				.build();
	}
}
