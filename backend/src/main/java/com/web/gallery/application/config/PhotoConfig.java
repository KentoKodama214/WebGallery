package com.web.gallery.application.config;

/** 写真に関するプロパティを取得するポート */
public interface PhotoConfig {

  /**
   * 写真一覧で、1ページあたりの表示枚数を取得する
   *
   * @return 1ページあたりの表示枚数
   */
  Integer getPhotoCountPerPage();

  /**
   * 最大ファイルサイズ（MB）を取得する
   *
   * @return 最大ファイルサイズ（MB）
   */
  Integer getMaxFileSizeMb();

  /**
   * mini-userの写真登録上限枚数を取得する
   *
   * @return mini-userの写真登録上限枚数
   */
  Integer getMiniUserUpperLimit();

  /**
   * normal-userの写真登録上限枚数を取得する
   *
   * @return normal-userの写真登録上限枚数
   */
  Integer getNormalUserUpperLimit();
}
