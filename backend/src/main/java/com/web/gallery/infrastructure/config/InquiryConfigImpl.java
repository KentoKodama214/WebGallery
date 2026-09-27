package com.web.gallery.infrastructure.config;

import com.web.gallery.application.config.InquiryConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** application.ymlのお問い合わせに関するプロパティを保持するConfigクラス */
@RequiredArgsConstructor
@Getter
@ConfigurationProperties(prefix = "app.inquiry")
public class InquiryConfigImpl implements InquiryConfig {
  /** お問い合わせ一覧で、1ページあたりの表示件数 */
  private final Integer inquiryCountPerPage;
}
