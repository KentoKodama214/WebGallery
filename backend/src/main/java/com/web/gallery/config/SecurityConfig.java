package com.web.gallery.config;

import com.web.gallery.constant.ApiRoutes;
import jakarta.servlet.DispatcherType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Securityで必要なオブジェクトを生成するConfigクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  private final RateLimitFilter rateLimitFilter;

  private final CorsConfig corsConfig;

  private final RestAccessDeniedHandler restAccessDeniedHandler;

  private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

  /**
   * bcryptアルゴリズムでハッシュ化を行うエンコーダのオブジェクトを生成します
   *
   * @return PasswordEncoderオブジェクト
   */
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * AuthenticationManagerのBeanを公開します
   *
   * @param authenticationConfiguration 認証設定
   * @return AuthenticationManagerオブジェクト
   * @throws Exception
   */
  @Bean
  AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  /**
   * CORS設定を定義します
   *
   * <p>標準構成では同一オリジンの `/api` プロキシ経由でアクセスするためCORSは発動しないが、 `NEXT_PUBLIC_API_BASE_URL`
   * で別オリジンのバックエンドを直接叩く構成に備え、必要最小限のみ許可する。 許可オリジンは環境変数 `FRONTEND_ORIGIN`
   * の値に限定し、許可ヘッダー・メソッドもクライアントが実際に 使用するものだけに絞る（`OPTIONS` はプリフライトとして自動的に許可されるため列挙しない）。
   *
   * @return CorsConfigurationSourceオブジェクト
   */
  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(corsConfig.getAllowedOrigins());
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  /**
   * APIレスポンスに付与するセキュリティヘッダーを設定します
   *
   * <p>フロント（Next.js）のページレスポンスには `next.config.ts` / `src/proxy.ts` でヘッダーを付与しているが、
   * バックエンドを直接叩く経路（別オリジン構成・前段プロキシのすり抜け等）に備えた多層防御として、 バックエンド側にも付与する。APIはJSONのみを返すため CSP は
   * `default-src 'none'` まで絞れる。 HSTS は HTTPS リクエスト（ALB 経由の `x-forwarded-proto: https`）に対してのみ送出される。
   *
   * @param headers ヘッダー設定オブジェクト
   */
  private static void applyApiResponseHeaders(HeadersConfigurer<HttpSecurity> headers) {
    headers
        .httpStrictTransportSecurity(
            hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(63_072_000))
        .referrerPolicy(
            referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
        .contentSecurityPolicy(
            csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'"))
        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny);
  }

  /**
   * OpenAPIドキュメント用のSecurityFilterChainを生成します
   *
   * <p>API仕様の露出を避けるため、本番プロファイル（prod）では登録せず、 デフォルトのSecurityFilterChainにより拒否される
   *
   * @param http HTTPセキュリティオブジェクト
   * @return SecurityFilterChainオブジェクト
   * @throws Exception
   */
  @Bean
  @Order(0)
  @Profile("!prod")
  SecurityFilterChain openApiSecurityFilterChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/v3/api-docs/**", "/scalar/**")
        .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
        .csrf(csrf -> csrf.disable());
    return http.build();
  }

  /**
   * API用のSecurityFilterChainを生成します（JWT認証、ステートレス）
   *
   * @param http HTTPセキュリティオブジェクト
   * @return SecurityFilterChainオブジェクト
   * @throws Exception
   */
  @Bean
  @Order(1)
  SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/api/**")
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .headers(SecurityConfig::applyApiResponseHeaders)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorizeRequests ->
                authorizeRequests
                    // 認証API
                    .requestMatchers(ApiRoutes.API_AUTH_LOGIN)
                    .permitAll()
                    .requestMatchers(ApiRoutes.API_AUTH_REFRESH)
                    .permitAll()
                    .requestMatchers(ApiRoutes.API_AUTH_LOGOUT)
                    .permitAll()
                    // アカウント登録（POST）とアカウント一覧（GET）は公開
                    .requestMatchers(HttpMethod.GET, ApiRoutes.API_ACCOUNTS)
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, ApiRoutes.API_ACCOUNTS)
                    .permitAll()
                    // 写真一覧・詳細の閲覧（GET）は公開
                    .requestMatchers(HttpMethod.GET, ApiRoutes.API_PHOTOS)
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, ApiRoutes.API_PHOTO_DETAIL)
                    .permitAll()
                    // 都道府県一覧は公開
                    .requestMatchers(ApiRoutes.API_PREFECTURES)
                    .permitAll()
                    // 管理者APIはADMINロール必須（AOPアスペクトに加えた多層防御）
                    .requestMatchers(ApiRoutes.API_ADMIN_PREFIX + "/**")
                    .hasRole("ADMIN")
                    // それ以外は認証必須
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            exceptionHandling ->
                exceptionHandling
                    .authenticationEntryPoint(restAuthenticationEntryPoint)
                    .accessDeniedHandler(restAccessDeniedHandler))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        // レート制限は認証・認可より前に適用し、上限超過リクエストで無駄な認証処理をさせない
        .addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class);

    return http.build();
  }

  /**
   * {@link RateLimitFilter} のサーブレットコンテナへの自動登録を無効化します
   *
   * <p>{@code @Component} かつ {@code Filter} であるため Spring Boot が全URL向けに自動登録するが、 適用対象は {@code
   * SecurityFilterChain} 内に {@code addFilterBefore} で差し込んだ1インスタンスに限定する。
   *
   * @param filter レート制限フィルター
   * @return 自動登録を無効化した登録Bean
   */
  @Bean
  FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RateLimitFilter filter) {
    FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
  }

  /**
   * どのSecurityFilterChainにもマッチしないリクエスト用のデフォルトSecurityFilterChainを生成します
   *
   * <p>マッチするチェーンが無いとリクエストがセキュリティ処理を経ずに通過するため、 明示的にすべて拒否する（{@code /error}等のディスパッチは通す）
   *
   * @param http HTTPセキュリティオブジェクト
   * @return SecurityFilterChainオブジェクト
   * @throws Exception
   */
  @Bean
  @Order(Integer.MAX_VALUE)
  SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .headers(SecurityConfig::applyApiResponseHeaders)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .dispatcherTypeMatchers(
                        DispatcherType.ERROR, DispatcherType.ASYNC, DispatcherType.FORWARD)
                    .permitAll()
                    .anyRequest()
                    .denyAll());
    return http.build();
  }
}
