package com.web.gallery.service;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.model.common.LocationModelList;

/** ロケーションに関するビジネスロジックを扱うServiceクラス */
public interface LocationService {
  /**
   * アカウントが登録済みのロケーション一覧を取得する
   *
   * @param accountNo アカウント番号
   * @return {@link LocationModelList}
   */
  LocationModelList getLocationList(AccountNo accountNo);
}
