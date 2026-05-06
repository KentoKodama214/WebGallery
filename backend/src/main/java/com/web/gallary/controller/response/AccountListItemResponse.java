package com.web.gallary.controller.response;

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
}
