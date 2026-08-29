package com.web.gallery.controller.response;

import java.util.List;

import com.web.gallery.model.AccountPageModel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 管理者用アカウント一覧のレスポンスパラメータを保持するクラス
 */
@Schema(description = "管理者用アカウント一覧レスポンス")
@Data
@Builder
public class AdminAccountListGetResponse {
	/** 最後まで取得できたか */
	@Schema(description = "最後のページかどうか")
	private Boolean isLast;

	/** アカウント一覧 */
	@Schema(description = "アカウント一覧")
	private List<AdminAccountListItemResponse> accountList;

	/**
	 * AccountPageModelからAdminAccountListGetResponseを生成する
	 *
	 * @param	accountPageModel	{@link AccountPageModel}
	 * @return						{@link AdminAccountListGetResponse}
	 */
	public static AdminAccountListGetResponse from(AccountPageModel accountPageModel) {
		List<AdminAccountListItemResponse> accountListItemResponseList = accountPageModel.getAccountModelList().stream()
				.map(AdminAccountListItemResponse::from)
				.toList();

		return AdminAccountListGetResponse.builder()
				.isLast(accountPageModel.getIsLast())
				.accountList(accountListItemResponseList)
				.build();
	}
}
