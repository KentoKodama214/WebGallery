package com.web.gallary.controller.response;

import lombok.Builder;
import lombok.Data;

/**
 * アカウント更新のレスポンスパラメータを保持するクラス
 */
@Data
@Builder
public class AccountUpdateResponse {
	/** HTTPステータス */
	private Integer httpStatus;

	/** アカウントIDが重複しているか */
	private Boolean isDuplicateAccountId;

	/** アカウントIDが更新されたか */
	private Boolean isAccountIdChanged;
	
	/** パスワードが更新されたか */
	private Boolean isPasswordChanged;
	
	/** メッセージ */
	private String message;
}