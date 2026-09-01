package com.web.gallery.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.context.annotation.Lazy;

import com.web.gallery.AccountPrincipal;
import com.web.gallery.helper.AuthenticatedUserCache;
import com.web.gallery.helper.JwtTokenProvider;
import com.web.gallery.service.impl.AccountServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWTトークンを検証し、認証情報をSecurityContextに設定するフィルタークラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;
	private final AccountServiceImpl accountServiceImpl;
	private final AuthenticatedUserCache authenticatedUserCache;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, @Lazy AccountServiceImpl accountServiceImpl,
			AuthenticatedUserCache authenticatedUserCache) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.accountServiceImpl = accountServiceImpl;
		this.authenticatedUserCache = authenticatedUserCache;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		String authHeader = request.getHeader(AUTHORIZATION_HEADER);

		if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = authHeader.substring(BEARER_PREFIX.length());

		if (jwtTokenProvider.isTokenValid(token)) {
			String accountId = jwtTokenProvider.getAccountIdFromToken(token);

			if (SecurityContextHolder.getContext().getAuthentication() == null) {
				// 毎リクエストのDB参照を間引くためごく短時間キャッシュする
				// （ロック・削除の反映は最大でキャッシュTTL（既定10秒）ぶん遅延する）
				AccountPrincipal userDetails = authenticatedUserCache.get(accountId,
						() -> (AccountPrincipal) accountServiceImpl.loadUserByUsername(accountId));

				// アクセストークン自体は失効まで有効だが、その間に管理者ロック・アカウント削除が
				// 行われた場合は即座に認証を無効化する（トークン署名・有効期限だけを信頼しない）
				if (userDetails.isEnabled() && userDetails.isAccountNonLocked()
						&& userDetails.isAccountNonExpired() && userDetails.isCredentialsNonExpired()) {
					UsernamePasswordAuthenticationToken authToken =
							new UsernamePasswordAuthenticationToken(
									userDetails, null, userDetails.getAuthorities());
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			}
		}

		filterChain.doFilter(request, response);
	}
}
