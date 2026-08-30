package com.web.gallery.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * スケジュール実行（{@code @Scheduled}）を有効化するConfigクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
