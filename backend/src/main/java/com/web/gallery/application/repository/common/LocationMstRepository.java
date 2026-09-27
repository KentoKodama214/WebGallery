package com.web.gallery.application.repository.common;

import com.web.gallery.application.model.common.LocationModelList;
import com.web.gallery.domain.model.account.AccountNo;

/** ロケーションマスタデータを永続化するRepositoryクラス */
public interface LocationMstRepository {
  /**
   * アカウントが登録済みのロケーション一覧を取得する
   *
   * @param accountNo アカウント番号
   * @return {@link LocationModelList}
   */
  LocationModelList getListByAccount(AccountNo accountNo);
}
