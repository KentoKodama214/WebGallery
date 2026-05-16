package com.web.gallary.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.web.gallary.constant.ApiRoutes;

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
	SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
		http.securityMatcher("/api/**")
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(csrf -> csrf.disable())
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorizeRequests -> authorizeRequests
				// 認証API
				.requestMatchers(ApiRoutes.API_AUTH_LOGIN).permitAll()
				.requestMatchers(ApiRoutes.API_AUTH_REFRESH).permitAll()
				.requestMatchers(ApiRoutes.API_AUTH_LOGOUT).permitAll()
				// アカウント登録（POST）とアカウント一覧（GET）は公開
				.requestMatchers(HttpMethod.GET, ApiRoutes.API_ACCOUNTS).permitAll()
				.requestMatchers(HttpMethod.POST, ApiRoutes.API_ACCOUNTS).permitAll()
				// 写真一覧・詳細の閲覧（GET）は公開
				.requestMatchers(HttpMethod.GET, ApiRoutes.API_PHOTOS).permitAll()
				.requestMatchers(HttpMethod.GET, ApiRoutes.API_PHOTO_DETAIL).permitAll()
				// 都道府県一覧は公開
				.requestMatchers(ApiRoutes.API_PREFECTURES).permitAll()
				// それ以外は認証必須
				.anyRequest().authenticated())
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

}
