package com.web.gallery.domain.exception;

/**
 * リカバリーコードによる本人確認に失敗した場合のExceptionクラス
 *
 * <p>失敗理由（アカウントが存在しない・コードが一致しない・使用済み）はメッセージとログにのみ残し、 クライアントへは区別せず一律の応答を返す（アカウントの存在やコードの状態を推測させないため）
 */
public class InvalidRecoveryCodeException extends RuntimeException {
  public InvalidRecoveryCodeException(String message) {
    super(message);
  }
}
