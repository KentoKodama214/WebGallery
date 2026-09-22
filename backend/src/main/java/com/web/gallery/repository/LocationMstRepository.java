package com.web.gallery.repository;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.model.common.LocationModelList;

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
