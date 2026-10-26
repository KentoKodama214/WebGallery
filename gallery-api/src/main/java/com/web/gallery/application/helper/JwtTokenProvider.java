package com.web.gallery.application.helper;

import com.web.gallery.application.AccountPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

/**
 * JWTトークンの生成・検証を行うインターフェース
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
public interface JwtTokenProvider {

  /**
   * アクセストークンを生成する
   *
   * @param principal 認証済みユーザー情報
   * @return JWTアクセストークン文字列
   */
  String generateAccessToken(AccountPrincipal principal);

  /**
   * リフレッシュトークンを生成する
   *
   * @return ランダムなUUID文字列
   */
  String generateRefreshToken();

  /**
   * アクセストークンを検証し、クレームを返す
   *
   * @param token JWTアクセストークン
   * @return トークンのクレーム
   * @throws JwtException トークンが無効な場合
   */
  Claims validateAccessToken(String token);

  /**
   * トークンからアカウントIDを取得する
   *
   * @param token JWTアクセストークン
   * @return アカウントID
   */
  String getAccountIdFromToken(String token);

  /**
   * トークンが有効かどうかを検証する
   *
   * @param token JWTアクセストークン
   * @return 有効な場合true
   */
  boolean isTokenValid(String token);
}
