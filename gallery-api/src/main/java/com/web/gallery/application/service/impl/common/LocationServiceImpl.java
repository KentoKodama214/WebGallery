package com.web.gallery.application.service.impl.common;

import com.web.gallery.application.model.common.LocationModelList;
import com.web.gallery.application.repository.common.LocationMstRepository;
import com.web.gallery.application.service.common.LocationService;
import com.web.gallery.domain.model.account.AccountNo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** ロケーションに関するビジネスロジックを扱うServiceの実装クラス */
@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

  private final LocationMstRepository locationMstRepository;

  /**
   * アカウントが登録済みのロケーション一覧を取得する
   *
   * @param accountNo アカウント番号
   * @return {@link LocationModelList}
   */
  @Override
  @Transactional(readOnly = true)
  public LocationModelList getLocationList(AccountNo accountNo) {
    return locationMstRepository.getListByAccount(accountNo);
  }
}
