package com.web.gallary.entity;

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
	private Integer tokenId;

	/** アカウント番号 */
	private Integer accountNo;

	/** トークンハッシュ */
	private String tokenHash;

	/** 有効期限 */
	private OffsetDateTime expiresAt;

	/** 作成日時 */
	private OffsetDateTime createdAt;

	/** 無効化フラグ */
	private Boolean isRevoked;
}
