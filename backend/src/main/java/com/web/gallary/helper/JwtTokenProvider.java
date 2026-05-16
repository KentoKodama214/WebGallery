package com.web.gallary.helper;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.web.gallary.AccountPrincipal;
import com.web.gallary.config.JwtConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

/**
 * JWTトークンの��成・検証を行うヘルパークラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
	private final JwtConfig jwtConfig;

	/**
	 * アクセストークンを���成する
	 *
	 * @param	principal	認証済みユーザー情報
	 * @return				JWTアクセストークン文字列
	 */
	public String generateAccessToken(AccountPrincipal principal) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + jwtConfig.getAccessTokenExpirationMinutes() * 60 * 1000L);

		return Jwts.builder()
				.subject(principal.getUsername())
				.claim("accountNo", principal.getAccountNo())
				.claim("accountName", principal.getAccountName())
				.claim("role", principal.getAuthorities().iterator().next().getAuthority())
				.issuedAt(now)
				.expiration(expiry)
				.signWith(getSigningKey())
				.compact();
	}

	/**
	 * リフレッシュトークンを生成する
	 *
	 * @return	ランダムなUUID文字列
	 */
	public String generateRefreshToken() {
		return UUID.randomUUID().toString();
	}

	/**
	 * アクセストークンを検証し、クレームを返す
	 *
	 * @param	token	JWTアクセストークン
	 * @return			トークンのクレーム
	 * @throws	JwtException	トークンが無効な場合
	 */
	public Claims validateAccessToken(String token) {
		return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	/**
	 * トークンからアカウントIDを取得する
	 *
	 * @param	token	JWTアクセストークン
	 * @return			アカウントID
	 */
	public String getAccountIdFromToken(String token) {
		return validateAccessToken(token).getSubject();
	}

	/**
	 * トークンが有効かどうかを検証する
	 *
	 * @param	token	JWTアクセストークン
	 * @return			有効な場合true
	 */
	public boolean isTokenValid(String token) {
		try {
			validateAccessToken(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * 署名用のキーを取得��る
	 *
	 * @return	SecretKeyオブジェクト
	 */
	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
	}
}
