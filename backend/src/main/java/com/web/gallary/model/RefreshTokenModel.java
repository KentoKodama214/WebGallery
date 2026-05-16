package com.web.gallary.model;

import java.time.OffsetDateTime;

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
}
