package com.web.gallery.infrastructure.config;

import com.web.gallery.application.config.PortfolioConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** application.ymlのポートフォリオに関するプロパティを保持するConfigクラス */
@RequiredArgsConstructor
@Getter
@ConfigurationProperties(prefix = "app.portfolio")
public class PortfolioConfigImpl implements PortfolioConfig {
  /** mini-userのポートフォリオ作成数上限 */
  private final Integer miniUserUpperLimit;

  /** normal-userのポートフォリオ作成数上限 */
  private final Integer normalUserUpperLimit;

  /** ポートフォリオ一覧の1ページあたりの表示件数 */
  private final Integer portfolioCountPerPage;
}
