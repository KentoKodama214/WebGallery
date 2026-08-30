package com.web.gallery.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.web.gallery.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 有効期限切れのリフレッシュトークンを定期的に削除するスケジューラークラス<p>
 * 無効化済み・期限切れのレコードが蓄積してテーブルが肥大化するのを防ぐ
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

	private final AuthService authService;

	/**
	 * 有効期限切れのリフレッシュトークンを削除する（毎日04:00に実行）
	 */
	@Scheduled(cron = "0 0 4 * * *")
	public void purgeExpiredRefreshTokens() {
		log.info("Start purging expired refresh tokens.");
		authService.purgeExpiredRefreshTokens();
		log.info("Finished purging expired refresh tokens.");
	}
}
