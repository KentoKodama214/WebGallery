package com.web.gallery.application.config;

/** アカウントに関するプロパティを取得するポート */
public interface AccountConfig {

  /** アカウント一覧で、1ページあたりの表示件数を取得する */
  Integer getAccountCountPerPage();
}
