package com.web.gallery.controller.response;

import java.time.OffsetDateTime;

import com.web.gallery.enumuration.AuthorityEnum;
import com.web.gallery.model.AccountModel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 管理者用アカウント一覧のレスポンスパラメータを保持するクラス
 */
@Schema(description = "管理者用アカウント一覧アイテムレスポンス")
@Data
@Builder
public class AdminAccountListItemResponse {
	/** アカウント番号 */
	@Schema(description = "アカウント番号", example = "1")
	private Long accountNo;

	/** アカウントID */
	@Schema(description = "アカウントID", example = "testuser01")
	private String accountId;

	/** アカウント名 */
	@Schema(description = "アカウント名", example = "テストユーザー")
	private String accountName;

	/** 権限区分 */
	@Schema(description = "権限区分", example = "administrator")
	private String authorityKbn;

	/** 削除フラグ */
	@Schema(description = "削除フラグ", example = "false")
	private Boolean isDeleted;

	/** 最終ログイン日時 */
	@Schema(description = "最終ログイン日時")
	private OffsetDateTime lastLoginDatetime;

	/** ログイン失敗回数 */
	@Schema(description = "ログイン失敗回数", example = "0")
	private Integer loginFailureCount;

	/**
	 * AccountModelからAdminAccountListItemResponseを生成する
	 *
	 * @param	model	{@link AccountModel}
	 * @return			{@link AdminAccountListItemResponse}
	 */
	public static AdminAccountListItemResponse from(AccountModel model) {
		return AdminAccountListItemResponse.builder()
				.accountNo(model.getAccountNo().value())
				.accountId(model.getAccountId().value())
				.accountName(model.getAccountName().value())
				.authorityKbn(model.getAuthorityKbn().getDbValue())
				.isDeleted(model.getIsDeleted().value())
				.lastLoginDatetime(model.getLastLoginDatetime() != null ? model.getLastLoginDatetime().value() : null)
				.loginFailureCount(model.getLoginFailureCount() != null ? model.getLoginFailureCount().value() : null)
				.build();
	}
}
