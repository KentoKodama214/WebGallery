package com.web.gallery.service;

import com.web.gallery.model.KbnMstModelList;

/** 区分マスタに関するビジネスロジックを行うServiceクラス */
public interface KbnMstService {
  /**
   * 都道府県の区分マスタを取得する
   *
   * @return {@link KbnMstModelList}
   */
  KbnMstModelList getPrefectureList();
}
