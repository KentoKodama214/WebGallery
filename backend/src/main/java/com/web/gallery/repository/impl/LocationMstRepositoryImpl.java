package com.web.gallery.repository.impl;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.entity.common.LocationMstCondition;
import com.web.gallery.mapper.LocationMstMapper;
import com.web.gallery.model.common.LocationModelList;
import com.web.gallery.repository.LocationMstRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/** ロケーションマスタデータを永続化するRepositoryの実装クラス */
@Repository
@RequiredArgsConstructor
public class LocationMstRepositoryImpl implements LocationMstRepository {

  private final LocationMstMapper locationMstMapper;

  /**
   * アカウントが登録済みのロケーション一覧を取得する
   *
   * @param accountNo アカウント番号
   * @return {@link LocationModelList}
   */
  @Override
  public LocationModelList getListByAccount(AccountNo accountNo) {
    return LocationModelList.from(
        locationMstMapper.select(LocationMstCondition.byAccountNo(accountNo.value())));
  }
}
