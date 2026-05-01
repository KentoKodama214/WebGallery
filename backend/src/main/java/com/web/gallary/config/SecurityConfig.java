package com.web.gallary.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.web.gallary.constant.ApiRoutes;
import com.web.gallary.util.AccountUrlUtil;

/**
 * Spring Securityで必要なオブジェクトを生成するConfigクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	/**
	 * bcryptアルゴリズムでハッシュ化を行うエンコーダのオブジェクトを生成します
	 * @return PasswordEncoderオブジェクト
	 */
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * SecurityFilterChainのオブジェクトを生成します
	 * @param http	HTTPセキュリティオブジェクト
	 * @return		SecurityFilterChainオブジェクト
	 * @throws Exception
	 */
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(authorizeRequests -> authorizeRequests
				.requestMatchers("/css/**").permitAll()
				.requestMatchers("/js/**").permitAll()
				.requestMatchers("/image/**").permitAll()
				.requestMatchers("/").permitAll()
				.requestMatchers(ApiRoutes.HEADER).permitAll()
				.requestMatchers(ApiRoutes.FOOTER).permitAll()
				.requestMatchers(ApiRoutes.ERROR_PAGE).permitAll()
				.requestMatchers(ApiRoutes.LOGIN).permitAll()
				.requestMatchers(ApiRoutes.REGISTER).permitAll()
				.requestMatchers(ApiRoutes.API_ACCOUNTS).permitAll()
				.requestMatchers(ApiRoutes.API_ACCOUNTS + "/**").permitAll()
				.requestMatchers(ApiRoutes.ACCOUNT_LIST).permitAll()
				.requestMatchers(ApiRoutes.PHOTO + "/**").permitAll()
				.requestMatchers(ApiRoutes.API_PREFIX + "/**").permitAll()
				.requestMatchers(AccountUrlUtil.getAccountSettingUrl("{name}"))
					.access(new WebExpressionAuthorizationManager("#name == authentication.name"))
				.anyRequest().authenticated())
			.formLogin(formLogin -> formLogin
				.loginPage(ApiRoutes.LOGIN)
				.defaultSuccessUrl("/")
				.failureUrl(ApiRoutes.LOGIN)
				.permitAll())
			.sessionManagement(session -> session
				.invalidSessionUrl(ApiRoutes.LOGIN)
				.maximumSessions(1))
			.logout(logout -> logout
				.logoutRequestMatcher(new AntPathRequestMatcher(ApiRoutes.LOGOUT))
				.logoutSuccessUrl(ApiRoutes.LOGIN))
			.headers(headers -> headers
				// 外部サイトへの<iframe>の埋め込みは禁止し、自サイト内のみ許可する
				.frameOptions(frameOptions -> frameOptions.sameOrigin()));

		return http.build();
	}
}