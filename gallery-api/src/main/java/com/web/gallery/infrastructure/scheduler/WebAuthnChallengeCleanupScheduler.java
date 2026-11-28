package com.web.gallery.infrastructure.scheduler;

import com.web.gallery.application.service.auth.AuthService;
import com.web.gallery.domain.constant.Consts;
import com.web.gallery.infrastructure.scheduler.lock.SchedulerLock;
import com.web.gallery.infrastructure.scheduler.lock.SchedulerLockNameEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 有効期限切れのWebAuthnチャレンジを定期的に削除するスケジューラークラス
 *
 * <p>パスキーの登録・認証のために発行され、使われないまま期限切れになったチャレンジが蓄積して テーブルが肥大化するのを防ぐ。認証用チャレンジは未ログインでも発行できるため、
 * 日次ではなく毎時削除する（増加量はレート制限で抑えている）。
 *
 * <p>複数インスタンス構成では {@link SchedulerLock} によるアドバイザリーロックで多重実行を防止する。 個別インスタンスでスケジュール自体を無効化したい場合は {@code
 * app.scheduler.webauthn-challenge-cleanup-enabled=false} を指定する（未設定時は有効）。
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "app.scheduler",
    name = "webauthn-challenge-cleanup-enabled",
    matchIfMissing = true)
public class WebAuthnChallengeCleanupScheduler {

  private final SchedulerLock schedulerLock;
  private final AuthService authService;

  /** 有効期限切れのWebAuthnチャレンジを削除する（毎時15分 JSTに実行） */
  @Scheduled(cron = "0 15 * * * *", zone = Consts.ZONE_ID_ASIA_TOKYO)
  public void purgeExpiredWebAuthnChallenges() {
    schedulerLock.runIfLocked(
        SchedulerLockNameEnum.WEBAUTHN_CHALLENGE_CLEANUP,
        authService::purgeExpiredWebAuthnChallenges);
  }
}
