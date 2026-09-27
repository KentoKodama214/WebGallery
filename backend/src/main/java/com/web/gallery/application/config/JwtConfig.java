package com.web.gallery.application.config;

/** JWTに関するプロパティを取得するポート */
public interface JwtConfig {

  /**
   * JWTシークレットキーを取得する
   *
   * @return JWTシークレットキー
   */
  String getSecret();

  /**
   * アクセストークン有効期限（分）を取得する
   *
   * @return アクセストークン有効期限（分）
   */
  Integer getAccessTokenExpirationMinutes();

  /**
   * リフレッシュトークン有効期限（日）を取得する
   *
   * @return リフレッシュトークン有効期限（日）
   */
  Integer getRefreshTokenExpirationDays();

  /**
   * リフレッシュトークンのローテーション直後の猶予時間（秒）を取得する
   *
   * <p>ローテーション済み（無効化済み）トークンが再送されても、ローテーションから この秒数以内であれば「複数タブ・ネットワーク瞬断による正常系のリトライ」とみなし、
   * 全セッション失効（盗用対応）は行わず当該リクエストのみ拒否する。 この時間を超えた無効化済みトークンの再利用は盗用の疑いとして全セッションを失効させる。
   *
   * @return リフレッシュトークンのローテーション直後の猶予時間（秒）
   */
  Integer getRefreshTokenReuseGraceSeconds();
}
