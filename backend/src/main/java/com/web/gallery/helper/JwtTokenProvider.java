package com.web.gallery.helper;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import com.web.gallery.AccountPrincipal;
import com.web.gallery.config.JwtConfig;

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
	/** トークンの発行者（issuerクレーム）。発行・検証の双方で一致を要求する */
	private static final String ISSUER = "web-gallery";

	/** HS256で必要となるシークレットキーの最小バイト長（256bit） */
	private static final int MIN_SECRET_BYTE_LENGTH = 32;

	private final JwtConfig jwtConfig;

	/**
	 * 起動時にJWTシークレットキーの長さを検証する<p>
	 * HS256では256bit以上の鍵長が必須であり、短い鍵だと総当たりで署名偽造が可能になる
	 *
	 * @throws	IllegalStateException	シークレットキーが未設定または短すぎる場合
	 */
	@PostConstruct
	void validateSecret() {
		String secret = jwtConfig.getSecret();
		if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTE_LENGTH) {
			throw new IllegalStateException(
					"JWTシークレットキー（app.jwt.secret）は256bit（32バイト）以上である必要があります");
		}
	}

	/**
	 * アクセストークンを���成する
	 *
	 * @param	principal	認証済みユーザー情報
	 * @return				JWTアクセストークン文字列
	 */
	public String generateAccessToken(AccountPrincipal principal) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + jwtConfig.getAccessTokenExpirationMinutes() * 60 * 1000L);

		// クライアント保存されるトークンに含める情報は最小限に留める（氏名等のPIIは載せない）
		return Jwts.builder()
				.issuer(ISSUER)
				.subject(principal.getUsername())
				.claim("accountNo", principal.getAccountNo())
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
				.requireIssuer(ISSUER)
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
