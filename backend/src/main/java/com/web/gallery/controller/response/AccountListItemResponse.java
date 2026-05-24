package com.web.gallery.controller.response;

import com.web.gallery.model.AccountModel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * アカウント一覧のレスポンスパラメータを保持するクラス
 */
@Schema(description = "アカウント一覧アイテムレスポンス")
@Data
@Builder
public class AccountListItemResponse {
	/** アカウントID */
	@Schema(description = "アカウントID", example = "testuser01")
	private String accountId;

	/** アカウント名 */
	@Schema(description = "アカウント名", example = "テストユーザー")
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
