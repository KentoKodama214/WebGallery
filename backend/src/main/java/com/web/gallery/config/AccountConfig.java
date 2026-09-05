package com.web.gallery.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** application.ymlのアカウントに関するプロパティを保持するConfigクラス */
@RequiredArgsConstructor
@Getter
@ConfigurationProperties(prefix = "app.account")
public class AccountConfig {
  /** アカウント一覧で、1ページあたりの表示件数 */
  private final Integer accountCountPerPage;
}
