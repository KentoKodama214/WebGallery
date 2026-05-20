package com.web.gallery.controller.response;

import org.springframework.http.HttpStatus;

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

	/**
	 * 成功レスポンスを生成する
	 *
	 * @param	isDuplicateAccountId	アカウントIDが重複しているか
	 * @param	isAccountIdChanged		アカウントIDが更新されたか
	 * @param	isPasswordChanged		パスワードが更新されたか
	 * @param	message					メッセージ
	 * @return							{@link AccountUpdateResponse}
	 */
	public static AccountUpdateResponse of(Boolean isDuplicateAccountId, Boolean isAccountIdChanged, Boolean isPasswordChanged, String message) {
		return AccountUpdateResponse.builder()
				.httpStatus(HttpStatus.OK.value())
				.isDuplicateAccountId(isDuplicateAccountId)
				.isAccountIdChanged(isAccountIdChanged)
				.isPasswordChanged(isPasswordChanged)
				.message(message)
				.build();
	}
}