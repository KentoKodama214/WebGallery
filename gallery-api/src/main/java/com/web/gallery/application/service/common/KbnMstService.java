package com.web.gallery.application.service.common;

import com.web.gallery.application.model.common.KbnMstModelList;

/** 区分マスタに関するビジネスロジックを行うServiceクラス */
public interface KbnMstService {
  /**
   * 都道府県の区分マスタを取得する
   *
   * @return {@link KbnMstModelList}
   */
  KbnMstModelList getPrefectureList();
}
