package com.web.gallery.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * スケジューラの多重実行防止に使うPostgreSQLアドバイザリーロックを操作するMapperクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface SchedulerLockMapper {
  /**
   * トランザクションレベルのアドバイザリーロックの取得を試みる（{@code pg_try_advisory_xact_lock}）
   *
   * <p>取得できなかった場合も待機せず即座に結果を返す。取得したロックは呼び出し元のトランザクション終了時 （コミットまたはロールバック）に自動的に解放される。
   *
   * @param lockKey ロックのキー値（{@code bigint}）
   * @return ロックを取得できた場合true、他セッションが保持中で取得できなかった場合false
   */
  public Boolean tryAdvisoryXactLock(@Param("lockKey") long lockKey);
}
