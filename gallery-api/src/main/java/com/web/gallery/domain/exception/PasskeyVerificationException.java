package com.web.gallery.domain.exception;

/**
 * パスキー（WebAuthn）の登録・認証レスポンスの検証に失敗した場合のExceptionクラス
 *
 * <p>失敗理由（署名不正・origin不一致・チャレンジ不一致・署名カウンタの巻き戻り等）はメッセージとログにのみ残し、
 * クライアントへは区別せず一律の応答を返す（攻撃者に検証のどの段階で失敗したかを教えないため）
 */
public class PasskeyVerificationException extends RuntimeException {
  public PasskeyVerificationException(String message) {
    super(message);
  }

  public PasskeyVerificationException(String message, Throwable cause) {
    super(message, cause);
  }
}
