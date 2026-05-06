package com.web.gallary.config;

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
}
