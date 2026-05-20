package com.web.gallery.model;

import java.time.OffsetDateTime;

import com.web.gallery.entity.RefreshToken;

import lombok.Builder;
import lombok.Value;

/**
 * リフレッシュトークン情報を受け渡すためのModelクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
@Value
@Builder
public class RefreshTokenModel {
	/** アカウント番号 */
	private Integer accountNo;

	/** トークンハッシュ */
	private String tokenHash;

	/** 有効期限 */
	private OffsetDateTime expiresAt;

	/** 無効化フラグ */
	private Boolean isRevoked;

	/**
	 * RefreshTokenエンティティからRefreshTokenModelを生成する
	 *
	 * @param	entity	{@link RefreshToken}
	 * @return			{@link RefreshTokenModel}
	 */
	public static RefreshTokenModel from(RefreshToken entity) {
		return RefreshTokenModel.builder()
				.accountNo(entity.getAccountNo())
				.tokenHash(entity.getTokenHash())
				.expiresAt(entity.getExpiresAt())
				.isRevoked(entity.getIsRevoked())
				.build();
	}
}
