package com.web.gallery.model;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.ExpiresAt;
import com.web.gallery.domain.common.IsRevoked;
import com.web.gallery.domain.common.TokenHash;
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
	private AccountNo accountNo;

	/** トークンハッシュ */
	private TokenHash tokenHash;

	/** 有効期限 */
	private ExpiresAt expiresAt;

	/** 無効化フラグ */
	private IsRevoked isRevoked;

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
