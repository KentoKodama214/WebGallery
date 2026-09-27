package com.web.gallery.infrastructure.config;

import com.web.gallery.application.config.AccountConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** application.ymlのアカウントに関するプロパティを保持するConfigクラス */
@RequiredArgsConstructor
@Getter
@ConfigurationProperties(prefix = "app.account")
public class AccountConfigImpl implements AccountConfig {
  /** アカウント一覧で、1ページあたりの表示件数 */
  private final Integer accountCountPerPage;
}
