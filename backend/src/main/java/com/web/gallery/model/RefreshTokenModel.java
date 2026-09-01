package com.web.gallery.model;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.ExpiresAt;
import com.web.gallery.domain.common.IsRevoked;
import com.web.gallery.domain.common.TokenHash;
import com.web.gallery.domain.common.UpdatedAt;
import com.web.gallery.entity.RefreshToken;

import lombok.Builder;
import lombok.NonNull;
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
	@NonNull
	private AccountNo accountNo;

	/** トークンハッシュ */
	@NonNull
	private TokenHash tokenHash;

	/** 有効期限 */
	@NonNull
	private ExpiresAt expiresAt;

	/** 無効化フラグ（{@link #of}生成時は未設定のためnull許容） */
	private IsRevoked isRevoked;

	/**
	 * 更新日時（ローテーション＝無効化した時刻。盗用検知の猶予判定に使用する。
	 * エンティティ由来の取得時のみ設定される）
	 */
	private UpdatedAt updatedAt;

	/**
	 * RefreshTokenエンティティからRefreshTokenModelを生成する
	 *
	 * @param	entity	{@link RefreshToken}
	 * @return			{@link RefreshTokenModel}
	 */
	public static RefreshTokenModel from(RefreshToken entity) {
		return RefreshTokenModel.builder()
				.accountNo(new AccountNo(entity.getAccountNo()))
				.tokenHash(new TokenHash(entity.getTokenHash()))
				.expiresAt(new ExpiresAt(entity.getExpiresAt()))
				.isRevoked(entity.getIsRevoked() != null ? new IsRevoked(entity.getIsRevoked()) : null)
				.updatedAt(entity.getUpdatedAt() != null ? new UpdatedAt(entity.getUpdatedAt()) : null)
				.build();
	}

	/**
	 * アカウント番号・トークンハッシュ・有効期限からRefreshTokenModelを生成する
	 *
	 * @param	accountNo	アカウント番号
	 * @param	tokenHash	トークンハッシュ
	 * @param	expiresAt	有効期限
	 * @return				{@link RefreshTokenModel}
	 */
	public static RefreshTokenModel of(AccountNo accountNo, TokenHash tokenHash, ExpiresAt expiresAt) {
		return RefreshTokenModel.builder()
				.accountNo(accountNo)
				.tokenHash(tokenHash)
				.expiresAt(expiresAt)
				.build();
	}
}
