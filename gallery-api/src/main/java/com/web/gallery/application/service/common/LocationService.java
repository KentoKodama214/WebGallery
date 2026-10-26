package com.web.gallery.application.service.common;

import com.web.gallery.application.model.common.LocationModelList;
import com.web.gallery.domain.model.account.AccountNo;

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
