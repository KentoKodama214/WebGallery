package com.web.gallery.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * application.ymlのJWTに関するプロパティを保持するConfigクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
@RequiredArgsConstructor
@Getter
@ConfigurationProperties(prefix = "app.jwt")
public class JwtConfig {
	/** JWTシークレットキー */
	private final String secret;

	/** アクセストークン有効期限（分） */
	private final Integer accessTokenExpirationMinutes;

	/** リフレッシュトークン有効期限（日） */
	private final Integer refreshTokenExpirationDays;

	/**
	 * リフレッシュトークンのローテーション直後の猶予時間（秒）<p>
	 * ローテーション済み（無効化済み）トークンが再送されても、ローテーションから
	 * この秒数以内であれば「複数タブ・ネットワーク瞬断による正常系のリトライ」とみなし、
	 * 全セッション失効（盗用対応）は行わず当該リクエストのみ拒否する。
	 * この時間を超えた無効化済みトークンの再利用は盗用の疑いとして全セッションを失効させる。
	 */
	private final Integer refreshTokenReuseGraceSeconds;
}
