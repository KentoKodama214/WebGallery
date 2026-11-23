package com.web.gallery.application.config;

import java.util.List;

/** パスキー（WebAuthn）に関するプロパティを取得するポート */
public interface WebAuthnConfig {

  /**
   * RP ID（パスキーを紐づけるドメイン）を取得する
   *
   * @return RP ID
   */
  String getRpId();

  /**
   * RP名（パスキーマネージャーに表示されるサービス名）を取得する
   *
   * @return RP名
   */
  String getRpName();

  /**
   * パスキーの登録・認証を許可するorigin（フロントエンドのオリジン）を取得する
   *
   * @return 許可するoriginの一覧
   */
  List<String> getAllowedOrigins();

  /**
   * チャレンジの有効期間（秒）を取得する
   *
   * <p>クライアントへ渡すタイムアウトにも同じ値を使う
   *
   * @return チャレンジの有効期間（秒）
   */
  Integer getChallengeTimeoutSeconds();
}
