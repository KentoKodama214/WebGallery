package com.web.gallary.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.context.annotation.Lazy;

import com.web.gallary.helper.JwtTokenProvider;
import com.web.gallary.service.impl.AccountServiceImpl;

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

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, @Lazy AccountServiceImpl accountServiceImpl) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.accountServiceImpl = accountServiceImpl;
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
				UserDetails userDetails = accountServiceImpl.loadUserByUsername(accountId);

				UsernamePasswordAuthenticationToken authToken =
						new UsernamePasswordAuthenticationToken(
								userDetails, null, userDetails.getAuthorities());
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				SecurityContextHolder.getContext().setAuthentication(authToken);
			}
		}

		filterChain.doFilter(request, response);
	}
}
