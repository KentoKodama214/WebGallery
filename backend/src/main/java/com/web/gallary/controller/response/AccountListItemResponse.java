package com.web.gallary.controller.response;

import com.web.gallary.model.AccountModel;

import lombok.Builder;
import lombok.Data;

/**
 * アカウント一覧のレスポンスパラメータを保持するクラス
 */
@Data
@Builder
public class AccountListItemResponse {
	/** アカウントID */
	private String accountId;

	/** アカウント名 */
	private String accountName;

	/**
	 * AccountModelからAccountListItemResponseを生成する
	 *
	 * @param	model	{@link AccountModel}
	 * @return			{@link AccountListItemResponse}
	 */
	public static AccountListItemResponse from(AccountModel model) {
		return AccountListItemResponse.builder()
				.accountId(model.getAccountId())
				.accountName(model.getAccountName())
				.build();
	}
}
