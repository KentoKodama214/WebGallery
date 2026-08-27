package com.web.gallery.entity;

import java.time.OffsetDateTime;

import com.web.gallery.model.RefreshTokenModel;

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
	private Long accountNo;

	/** トークンハッシュ */
	private String tokenHash;

	/** 有効期限 */
	private OffsetDateTime expiresAt;

	/** 作成日時 */
	private OffsetDateTime createdAt;

	/** 更新者 */
	private Long updatedBy;

	/** 更新日時 */
	private OffsetDateTime updatedAt;

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
				.accountNo(model.getAccountNo().value())
				.tokenHash(model.getTokenHash().value())
				.expiresAt(model.getExpiresAt().value())
				.updatedBy(model.getAccountNo().value())
				.build();
	}
}
