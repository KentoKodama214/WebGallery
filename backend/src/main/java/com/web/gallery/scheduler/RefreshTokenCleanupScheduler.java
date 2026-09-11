package com.web.gallery.scheduler;

import com.web.gallery.constant.Consts;
import com.web.gallery.enumeration.SchedulerLockName;
import com.web.gallery.helper.SchedulerLock;
import com.web.gallery.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 有効期限切れのリフレッシュトークンを定期的に削除するスケジューラークラス
 *
 * <p>無効化済み・期限切れのレコードが蓄積してテーブルが肥大化するのを防ぐ。
 *
 * <p>複数インスタンス構成では全インスタンスが同時刻に起動するため、{@link SchedulerLock} による PostgreSQL
 * アドバイザリーロックで多重実行を防止し、実際に削除を行うのは1インスタンスのみとする。 開始・完了・スキップのログは {@link SchedulerLock}
 * が出力する。個別インスタンスでスケジュール自体を無効化したい場合は {@code app.scheduler.refresh-token-cleanup-enabled=false}
 * を指定する（未設定時は有効）。
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "app.scheduler",
    name = "refresh-token-cleanup-enabled",
    matchIfMissing = true)
public class RefreshTokenCleanupScheduler {

  private final SchedulerLock schedulerLock;
  private final AuthService authService;

  /** 有効期限切れのリフレッシュトークンを削除する（毎日04:00 JSTに実行） */
  @Scheduled(cron = "0 0 4 * * *", zone = Consts.ZONE_ID_ASIA_TOKYO)
  public void purgeExpiredRefreshTokens() {
    schedulerLock.runIfLocked(
        SchedulerLockName.REFRESH_TOKEN_CLEANUP, authService::purgeExpiredRefreshTokens);
  }
}
