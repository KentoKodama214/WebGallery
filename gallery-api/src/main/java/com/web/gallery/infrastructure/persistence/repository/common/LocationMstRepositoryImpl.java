package com.web.gallery.infrastructure.persistence.repository.common;

import com.web.gallery.application.model.common.LocationModel;
import com.web.gallery.application.model.common.LocationModelList;
import com.web.gallery.application.repository.common.LocationMstRepository;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.common.Address;
import com.web.gallery.domain.model.common.GeoLocation;
import com.web.gallery.domain.model.common.Latitude;
import com.web.gallery.domain.model.common.LocationDisplayName;
import com.web.gallery.domain.model.common.LocationManagementName;
import com.web.gallery.domain.model.common.Longitude;
import com.web.gallery.domain.model.photo.LocationNo;
import com.web.gallery.infrastructure.persistence.entity.common.LocationMst;
import com.web.gallery.infrastructure.persistence.entity.common.LocationMstCondition;
import com.web.gallery.infrastructure.persistence.mapper.common.LocationMstMapper;
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
    return LocationModelList.of(
        locationMstMapper.select(LocationMstCondition.byAccountNo(accountNo.value())).stream()
            .map(this::toLocationModel)
            .toList());
  }

  /**
   * LocationMstエンティティからLocationModelを組み立てる
   *
   * @param entity {@link LocationMst}
   * @return {@link LocationModel}
   */
  private LocationModel toLocationModel(LocationMst entity) {
    return LocationModel.builder()
        .accountNo(new AccountNo(entity.getAccountNo()))
        .locationNo(new LocationNo(entity.getLocationNo()))
        .managementName(new LocationManagementName(entity.getManagementName()))
        .displayName(new LocationDisplayName(entity.getDisplayName()))
        .geoLocation(
            new GeoLocation(
                new Address(entity.getAddress()),
                new Latitude(entity.getLatitude()),
                new Longitude(entity.getLongitude())))
        .build();
  }
}
