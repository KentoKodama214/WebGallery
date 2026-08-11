package com.web.gallery.entity;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.CreatedAt;
import com.web.gallery.model.RefreshTokenModel;

import java.time.OffsetDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * リフレッシュトークンテーブルのEntityクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
@Data
@Builder
public class RefreshToken {
	/** トークンID */
	private Long tokenId;

	/** アカウント番号 */
	private AccountNo accountNo;

	/** トークンハッシュ */
	private String tokenHash;

	/** 有効期限 */
	private OffsetDateTime expiresAt;

	/** 作成日時 */
	private CreatedAt createdAt;

	/** 無効化フラグ */
	private Boolean isRevoked;

	/**
	 * RefreshTokenModelからRefreshTokenエンティティを生成する
	 *
	 * @param	model	{@link RefreshTokenModel}
	 * @return			{@link RefreshToken}
	 */
	public static RefreshToken from(RefreshTokenModel model) {
		return RefreshToken.builder()
				.accountNo(model.getAccountNo())
				.tokenHash(model.getTokenHash())
				.expiresAt(model.getExpiresAt())
				.build();
	}
}
