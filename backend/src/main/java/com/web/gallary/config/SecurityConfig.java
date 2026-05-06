package com.web.gallary.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.web.gallary.constant.ApiRoutes;
import com.web.gallary.util.AccountUrlUtil;

import lombok.RequiredArgsConstructor;

/**
 * Spring Securityで必要なオブジェクトを生成するConfigクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	/**
	 * bcryptアルゴリズムでハッシュ化を行うエンコーダのオブジェクトを生成します
	 * @return PasswordEncoderオブジェクト
	 */
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * AuthenticationManagerのBeanを公開します
	 * @param	authenticationConfiguration	認証設定
	 * @return		AuthenticationManagerオブジェクト
	 * @throws Exception
	 */
	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	/**
	 * CORS設定を定義します
	 * @return	CorsConfigurationSourceオブジェクト
	 */
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of("http://localhost:3000"));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	/**
	 * API用のSecurityFilterChainを生成します（JWT認証、ステートレス）
	 * @param	http	HTTPセキュリティオブジェクト
	 * @return			SecurityFilterChainオブジェクト
	 * @throws Exception
	 */
	@Bean
	@Order(1)
	SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
		http.securityMatcher("/api/**")
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(csrf -> csrf.disable())
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorizeRequests -> authorizeRequests
				.requestMatchers(ApiRoutes.API_AUTH_LOGIN).permitAll()
				.requestMatchers(ApiRoutes.API_AUTH_REFRESH).permitAll()
				.requestMatchers(ApiRoutes.API_AUTH_LOGOUT).permitAll()
				.requestMatchers(ApiRoutes.API_ACCOUNTS).permitAll()
				.requestMatchers(ApiRoutes.API_ACCOUNTS + "/**").permitAll()
				.requestMatchers(ApiRoutes.API_PREFECTURES).permitAll()
				.requestMatchers(ApiRoutes.API_FAVORITES).authenticated()
				.anyRequest().authenticated())
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/**
	 * MVC用のSecurityFilterChainを生成します（フォームログイン、セッション管理）
	 * @param http	HTTPセキュリティオブジェクト
	 * @return		SecurityFilterChainオブジェクト
	 * @throws Exception
	 */
	@Bean
	@Order(2)
	SecurityFilterChain mvcSecurityFilterChain(HttpSecurity http) throws Exception {
		http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.authorizeHttpRequests(authorizeRequests -> authorizeRequests
				.requestMatchers("/css/**").permitAll()
				.requestMatchers("/js/**").permitAll()
				.requestMatchers("/image/**").permitAll()
				.requestMatchers("/").permitAll()
				.requestMatchers(ApiRoutes.HEADER).permitAll()
				.requestMatchers(ApiRoutes.FOOTER).permitAll()
				.requestMatchers(ApiRoutes.ERROR_PAGE).permitAll()
				.requestMatchers(ApiRoutes.LOGIN).permitAll()
				.requestMatchers(ApiRoutes.REGISTER).permitAll()
				.requestMatchers(ApiRoutes.ACCOUNT_LIST).permitAll()
				.requestMatchers(ApiRoutes.PHOTO + "/**").permitAll()
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
				.frameOptions(frameOptions -> frameOptions.sameOrigin()));

		return http.build();
	}
}
