package com.web.gallery.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 複数インスタンス構成でスケジューラの多重実行を防ぐためのロック名を管理するEnumクラス
 *
 * <p>各要素は PostgreSQL のアドバイザリーロック（{@code pg_try_advisory_xact_lock}）で使用する {@code bigint}
 * のキー値と、ログ出力用の日本語名称を保持する。キー値はインスタンス間・再起動をまたいで安定させる必要があるため、 一度割り当てた値は変更しないこと。
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum SchedulerLockName {
  /** 有効期限切れリフレッシュトークンの削除処理 */
  REFRESH_TOKEN_CLEANUP(4_001L, "有効期限切れリフレッシュトークンの削除");

  /** アドバイザリーロックのキー値（{@code bigint}） */
  private final long lockKey;

  /** ログ出力に用いる日本語の名称 */
  private final String displayName;
}
