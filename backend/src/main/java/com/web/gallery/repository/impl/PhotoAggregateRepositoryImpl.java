package com.web.gallery.repository.impl;

import com.web.gallery.aggregate.Photo;
import com.web.gallery.domain.common.GeoLocation;
import com.web.gallery.domain.common.LocationManagementName;
import com.web.gallery.domain.photo.LocationNo;
import com.web.gallery.entity.common.LocationMst;
import com.web.gallery.entity.common.LocationMstCondition;
import com.web.gallery.entity.photo.PhotoFavoriteCondition;
import com.web.gallery.entity.photo.PhotoMst;
import com.web.gallery.entity.photo.PhotoMstCondition;
import com.web.gallery.entity.photo.PhotoMstUpdateTarget;
import com.web.gallery.entity.photo.PhotoTagMst;
import com.web.gallery.entity.photo.PhotoTagMstCondition;
import com.web.gallery.enumeration.ErrorEnum;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.mapper.LocationMstMapper;
import com.web.gallery.mapper.PhotoFavoriteMapper;
import com.web.gallery.mapper.PhotoMstMapper;
import com.web.gallery.mapper.PhotoTagMstMapper;
import com.web.gallery.model.photo.PhotoDeleteModel;
import com.web.gallery.model.photo.PhotoDetailModel;
import com.web.gallery.model.photo.PhotoFavoriteDeleteModel;
import com.web.gallery.model.photo.PhotoTagDeleteModel;
import com.web.gallery.repository.PhotoAggregateRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

/**
 * 写真集約（{@link Photo}）を永続化するRepositoryの実装クラス
 *
 * <p>PhotoMst・PhotoTagMst・PhotoFavorite・LocationMstの4テーブルへの永続化を、写真の登録・更新・削除という
 * ユースケース単位で整合性のある1操作としてまとめる。他のRepositoryには依存せず、Mapperを直接操作する
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PhotoAggregateRepositoryImpl implements PhotoAggregateRepository {

  private final PhotoMstMapper photoMstMapper;
  private final PhotoTagMstMapper photoTagMstMapper;
  private final PhotoFavoriteMapper photoFavoriteMapper;
  private final LocationMstMapper locationMstMapper;

  /**
   * 写真集約を新規登録する
   *
   * @param photo {@link Photo}
   * @throws GalleryException 以下のいずれかに該当する場合 ・同じファイル名の写真が既に保存済みの場合 ・選択されたロケーションが本人所有でない場合
   *     ・ロケーションの登録に失敗した場合 ・登録に失敗した場合
   */
  @Override
  public void regist(Photo photo) throws GalleryException {
    String filename = photo.getImageFile().value().getOriginalFilename();
    if (photoMstMapper.isExistPhoto(PhotoMstCondition.forExistCheck(photo.getDetail()))) {
      log.warn("Duplicate image file (filename: {})", filename);
      throw ErrorEnum.DUPLICATE_PHOTO_FILE.toException();
    }

    photo.updateLocationNo(resolveLocationNo(photo.getDetail()));

    PhotoMst photoMst =
        PhotoMst.fromForRegist(
            photo.getDetail(), photo.getImageFilePath().value(), photo.getPhotoNo().value());
    try {
      photoMstMapper.insert(photoMst);
    } catch (DuplicateKeyException e) {
      log.warn(
          "PhotoMst: Duplicate Key (AccountNo: {}, PhotoNo: {})",
          photo.getAccountNo().value(),
          photo.getPhotoNo().value(),
          e);
      throw ErrorEnum.FAIL_TO_REGIST_PHOTO.toException();
    }

    registTags(photo);
  }

  /**
   * 写真集約を更新する
   *
   * @param photo {@link Photo}
   * @throws GalleryException 以下のいずれかに該当する場合 ・選択されたロケーションが本人所有でない場合 ・ロケーションの登録に失敗した場合 ・更新に失敗した場合
   */
  @Override
  public void update(Photo photo) throws GalleryException {
    photo.updateLocationNo(resolveLocationNo(photo.getDetail()));

    PhotoMstCondition condition =
        PhotoMstCondition.byAccountAndPhotoNotDeleted(
            photo.getAccountNo().value(), photo.getPhotoNo().value());
    PhotoMstUpdateTarget target = PhotoMstUpdateTarget.fromForUpdate(photo.getDetail());

    if (photoMstMapper.update(condition, target) < 1) {
      log.warn(
          "PhotoMst: Update Failed (AccountNo: {}, PhotoNo: {})",
          photo.getAccountNo().value(),
          photo.getPhotoNo().value());
      throw ErrorEnum.FAIL_TO_UPDATE_PHOTO.toException();
    }

    photoTagMstMapper.delete(
        PhotoTagMstCondition.from(
            PhotoTagDeleteModel.of(photo.getAccountNo(), photo.getPhotoNo())));
    registTags(photo);
  }

  /**
   * 写真集約を削除する
   *
   * <p>対象写真への全アカウントからのお気に入り・タグを削除したうえで写真マスタを論理削除する
   *
   * @param photo {@link Photo}
   * @throws GalleryException 削除に失敗した場合
   */
  @Override
  public void delete(Photo photo) throws GalleryException {
    PhotoFavoriteDeleteModel favoriteDeleteModel =
        PhotoFavoriteDeleteModel.builder()
            .favoritePhotoAccountNo(photo.getAccountNo())
            .favoritePhotoNo(photo.getPhotoNo())
            .build();
    photoFavoriteMapper.delete(PhotoFavoriteCondition.forClear(favoriteDeleteModel));

    photoTagMstMapper.delete(
        PhotoTagMstCondition.from(
            PhotoTagDeleteModel.of(photo.getAccountNo(), photo.getPhotoNo())));

    PhotoMstCondition condition =
        PhotoMstCondition.byAccountAndPhotoNotDeleted(
            photo.getAccountNo().value(), photo.getPhotoNo().value());
    PhotoDeleteModel photoDeleteModel =
        PhotoDeleteModel.builder()
            .accountNo(photo.getAccountNo())
            .photoNo(photo.getPhotoNo())
            .imageFilePath(photo.getImageFilePathForDelete())
            .build();
    PhotoMstUpdateTarget target = PhotoMstUpdateTarget.forDelete(photoDeleteModel);

    if (photoMstMapper.update(condition, target) < 1) {
      // 対象写真が存在しない（未登録・既に削除済み）＝404。競合ではないため409は返さない
      log.warn(
          "PhotoMst: Not Found for delete (AccountNo: {}, PhotoNo: {})",
          photo.getAccountNo().value(),
          photo.getPhotoNo().value());
      throw ErrorEnum.PHOTO_NOT_FOUND.toException();
    }
  }

  /**
   * 写真詳細情報からロケーション番号を解決する
   *
   * <p>解決順は以下の通り： 1. ロケーション番号が指定されている（既存マスタからの選択） → 本人所有のロケーションマスタに存在するか検証し、そのまま採用する 2.
   * ロケーション番号が未指定で管理名が指定されている（新規入力） → 同一管理名のロケーションマスタが既に存在すればそのロケーション番号を再利用し、存在しなければ新規に採番・登録する 3.
   * どちらも未指定（ロケーション未設定） → デフォルト値（0）を採用する
   *
   * @param detail {@link PhotoDetailModel}
   * @return 解決済みの{@link LocationNo}
   * @throws GalleryException 以下のいずれかに該当する場合 ・選択されたロケーション番号が本人所有のロケーションマスタに存在しない場合
   *     ・新規ロケーションの登録に失敗した場合
   */
  private LocationNo resolveLocationNo(PhotoDetailModel detail) throws GalleryException {
    Long accountNo = detail.getAccountNo().value();
    LocationNo requestedLocationNo = detail.getLocationNo();

    if (requestedLocationNo != null && requestedLocationNo.value() > 0) {
      boolean isOwned =
          !locationMstMapper
              .select(
                  LocationMstCondition.byAccountAndLocationNo(
                      accountNo, requestedLocationNo.value()))
              .isEmpty();
      if (!isOwned) {
        log.warn(
            "LocationMst: Not Found (AccountNo: {}, LocationNo: {})",
            accountNo,
            requestedLocationNo.value());
        throw ErrorEnum.LOCATION_NOT_FOUND.toException();
      }
      return requestedLocationNo;
    }

    LocationManagementName managementName = detail.getManagementName();
    if (managementName == null) {
      return LocationNo.getOrDefault(null);
    }

    List<LocationMst> matched =
        locationMstMapper.select(
            LocationMstCondition.byAccountAndManagementName(accountNo, managementName.value()));
    if (!matched.isEmpty()) {
      return new LocationNo(matched.get(0).getLocationNo());
    }

    GeoLocation geoLocation = detail.getGeoLocation();
    LocationNo newLocationNo = LocationNo.next(locationMstMapper.getMaxLocationNo(accountNo));
    LocationMst locationMst =
        LocationMst.fromForRegist(
            detail.getAccountNo(),
            newLocationNo,
            managementName,
            detail.getDisplayName(),
            geoLocation);
    try {
      locationMstMapper.insert(locationMst);
    } catch (DuplicateKeyException e) {
      log.warn(
          "LocationMst: Duplicate Key (AccountNo: {}, ManagementName: {})",
          accountNo,
          managementName.value(),
          e);
      throw ErrorEnum.FAIL_TO_REGIST_LOCATION.toException();
    }
    return newLocationNo;
  }

  /**
   * 写真タグを登録する
   *
   * @param photo {@link Photo}
   * @throws GalleryException 登録に失敗した場合
   */
  private void registTags(Photo photo) throws GalleryException {
    if (photo.getPhotoTagModelList() == null || photo.getPhotoTagModelList().isEmpty()) {
      return;
    }

    List<PhotoTagMst> photoTagMstList =
        photo.getPhotoTagModelList().stream().map(PhotoTagMst::from).toList();
    try {
      photoTagMstMapper.insertBulk(photoTagMstList);
    } catch (DuplicateKeyException e) {
      log.warn(
          "PhotoTagMst: Duplicate Key (AccountNo: {}, PhotoNo: {})",
          photo.getAccountNo().value(),
          photo.getPhotoNo().value(),
          e);
      throw ErrorEnum.FAIL_TO_REGIST_PHOTO_TAG.toException();
    }
  }
}
