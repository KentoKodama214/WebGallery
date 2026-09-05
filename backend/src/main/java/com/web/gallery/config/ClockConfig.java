package com.web.gallery.config;

import com.web.gallery.constant.Consts;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 現在時刻取得用のClockに関する設定クラス
 *
 * @author Kento Kodama
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
public class ClockConfig {
  /**
   * JST（日本標準時）に固定したClockを生成する
   *
   * @return {@link Clock}
   */
  @Bean
  public Clock clock() {
    return Clock.system(Consts.JST);
  }
}
