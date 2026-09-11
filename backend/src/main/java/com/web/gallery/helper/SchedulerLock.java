package com.web.gallery.helper;

import com.web.gallery.enumeration.SchedulerLockName;
import com.web.gallery.repository.SchedulerLockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 複数インスタンス構成でスケジューラ処理の多重実行を防ぐためのHelperクラス
 *
 * <p>PostgreSQL のトランザクションレベルのアドバイザリーロック（{@code pg_try_advisory_xact_lock}）を用いて、
 * 同一ロック名の処理を全インスタンス中で1つだけ実行させる。
 *
 * <p>ロックの取得と{@code task}の実行を同一トランザクションで行うため、ロックは{@code task}完了後の
 * コミット時まで保持される。ロックを取得できなかったインスタンスは{@code task}を実行せずスキップする。
 *
 * <p>開始・完了・スキップ・異常終了のライフサイクルログは本クラスで一元的に出力するため、 呼び出し側の{@code task}では業務固有の情報（処理件数など）のみをログ出力すればよい。
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulerLock {
  /** 定期実行ログの接頭辞 */
  private static final String LOG_PREFIX = "[定期実行]";

  private final SchedulerLockRepository schedulerLockRepository;

  /**
   * 指定したロックを取得できた場合のみ{@code task}を実行する
   *
   * <p>他インスタンスがロックを保持している場合は{@code task}を実行せず、待機もせずに戻る。 {@code
   * task}が例外を投げた場合はライフサイクルログを出力したうえで再スローする。
   *
   * @param lockName ロック名
   * @param task ロック取得時に実行する処理
   */
  @Transactional
  public void runIfLocked(SchedulerLockName lockName, Runnable task) {
    String name = lockName.getDisplayName();
    if (!schedulerLockRepository.tryLock(lockName)) {
      log.info("{} {} スキップ（他インスタンスが実行中）", LOG_PREFIX, name);
      return;
    }
    log.info("{} {} 開始", LOG_PREFIX, name);
    try {
      task.run();
      log.info("{} {} 完了", LOG_PREFIX, name);
    } catch (RuntimeException e) {
      log.error("{} {} 異常終了", LOG_PREFIX, name, e);
      throw e;
    }
  }
}
