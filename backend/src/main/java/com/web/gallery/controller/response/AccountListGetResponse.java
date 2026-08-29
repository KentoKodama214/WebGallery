package com.web.gallery.controller.response;

import java.util.List;

import com.web.gallery.model.AccountPageModel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * アカウント一覧のレスポンスパラメータを保持するクラス
 */
@Schema(description = "アカウント一覧レスポンス")
@Data
@Builder
public class AccountListGetResponse {
	/** 最後まで取得できたか */
	@Schema(description = "最後のページかどうか")
	private Boolean isLast;

	/** アカウント一覧 */
	@Schema(description = "アカウント一覧")
	private List<AccountListItemResponse> accountList;

	/**
	 * AccountPageModelからAccountListGetResponseを生成する
	 *
	 * @param	accountPageModel	{@link AccountPageModel}
	 * @return						{@link AccountListGetResponse}
	 */
	public static AccountListGetResponse from(AccountPageModel accountPageModel) {
		List<AccountListItemResponse> accountListItemResponseList = accountPageModel.getAccountModelList().stream()
				.map(AccountListItemResponse::from)
				.toList();

		return AccountListGetResponse.builder()
				.isLast(accountPageModel.getIsLast())
				.accountList(accountListItemResponseList)
				.build();
	}
}
