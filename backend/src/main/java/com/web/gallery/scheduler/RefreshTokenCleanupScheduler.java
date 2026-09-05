package com.web.gallery.scheduler;

import com.web.gallery.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 有効期限切れのリフレッシュトークンを定期的に削除するスケジューラークラス
 *
 * <p>無効化済み・期限切れのレコードが蓄積してテーブルが肥大化するのを防ぐ。
 *
 * <p>複数インスタンス構成では全インスタンスが同時刻に実行し重複するため（削除自体は冪等）、 {@code
 * app.scheduler.refresh-token-cleanup-enabled=false} で個別インスタンスの実行を抑止できる （未設定時は有効）
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "app.scheduler",
    name = "refresh-token-cleanup-enabled",
    matchIfMissing = true)
public class RefreshTokenCleanupScheduler {

  private final AuthService authService;

  /** 有効期限切れのリフレッシュトークンを削除する（毎日04:00に実行） */
  @Scheduled(cron = "0 0 4 * * *")
  public void purgeExpiredRefreshTokens() {
    log.info("Start purging expired refresh tokens.");
    authService.purgeExpiredRefreshTokens();
    log.info("Finished purging expired refresh tokens.");
  }
}
