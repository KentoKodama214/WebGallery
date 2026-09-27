package com.web.gallery.application.repository.common;

import com.web.gallery.application.model.common.KbnMstModelList;
import com.web.gallery.domain.model.common.KbnClassCode;

/** 区分マスタデータを永続化するRepositoryクラス */
public interface KbnMstRepository {
  /**
   * 区分クラスコードに該当する区分マスタの一覧を取得する
   *
   * @param kbnClassCode 区分クラスコード
   * @return {@link KbnMstModelList}
   */
  KbnMstModelList get(KbnClassCode kbnClassCode);
}
