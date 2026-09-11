package com.web.gallery.repository;

import com.web.gallery.enumeration.SchedulerLockName;

/**
 * スケジューラの多重実行防止用ロックを管理するRepositoryクラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
public interface SchedulerLockRepository {
  /**
   * 指定したロック名のトランザクションレベルのアドバイザリーロックの取得を試みる
   *
   * <p>取得できなかった場合も待機せず即座に結果を返す。取得したロックは呼び出し元のトランザクション終了時に自動的に解放される。 そのため本メソッドはトランザクション内から呼び出すこと。
   *
   * @param lockName ロック名
   * @return ロックを取得できた場合true、他インスタンスが保持中で取得できなかった場合false
   */
  boolean tryLock(SchedulerLockName lockName);
}
