package com.web.gallary.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * application.ymlのログインに関するプロパティを保持するConfigクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
@RequiredArgsConstructor
@Getter
@ConfigurationProperties(prefix="auth.login")
public class LoginConfig {
	/** ログイン失敗上限回数 */
	private final Integer failCount;
}