package com.web.gallery.infrastructure.persistence.repository;

import com.web.gallery.application.model.common.LocationModelList;
import com.web.gallery.application.repository.LocationMstRepository;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.infrastructure.persistence.entity.common.LocationMstCondition;
import com.web.gallery.infrastructure.persistence.mapper.LocationMstMapper;
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
