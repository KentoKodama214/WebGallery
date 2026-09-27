package com.web.gallery.application.config;

/** お問い合わせに関するプロパティを取得するポート */
public interface InquiryConfig {

  /**
   * お問い合わせ一覧で、1ページあたりの表示件数を取得する
   *
   * @return 1ページあたりの表示件数
   */
  Integer getInquiryCountPerPage();
}
