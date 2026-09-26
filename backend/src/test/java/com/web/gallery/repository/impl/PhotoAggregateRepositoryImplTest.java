package com.web.gallery.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.web.gallery.aggregate.Photo;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.Address;
import com.web.gallery.domain.common.GeoLocation;
import com.web.gallery.domain.common.Latitude;
import com.web.gallery.domain.common.LocationDisplayName;
import com.web.gallery.domain.common.LocationManagementName;
import com.web.gallery.domain.common.Longitude;
import com.web.gallery.domain.photo.ImageFile;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.LocationNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.photo.TagEnglishName;
import com.web.gallery.domain.photo.TagJapaneseName;
import com.web.gallery.entity.common.LocationMst;
import com.web.gallery.entity.common.LocationMstCondition;
import com.web.gallery.entity.photo.PhotoFavoriteCondition;
import com.web.gallery.entity.photo.PhotoMst;
import com.web.gallery.entity.photo.PhotoMstCondition;
import com.web.gallery.entity.photo.PhotoMstUpdateTarget;
import com.web.gallery.entity.photo.PhotoTagMst;
import com.web.gallery.entity.photo.PhotoTagMstCondition;
import com.web.gallery.exception.BadRequestException;
import com.web.gallery.exception.FileDuplicateException;
import com.web.gallery.exception.GalleryException;
import com.web.gallery.exception.PhotoNotFoundException;
import com.web.gallery.exception.RegistFailureException;
import com.web.gallery.exception.UpdateFailureException;
import com.web.gallery.mapper.LocationMstMapper;
import com.web.gallery.mapper.PhotoFavoriteMapper;
import com.web.gallery.mapper.PhotoMstMapper;
import com.web.gallery.mapper.PhotoTagMstMapper;
import com.web.gallery.model.photo.PhotoDetailModel;
import com.web.gallery.model.photo.PhotoTagModel;
import com.web.gallery.model.photo.PhotoTagModelList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PhotoAggregateRepositoryImplTest {
  @InjectMocks private PhotoAggregateRepositoryImpl photoAggregateRepositoryImpl;

  @Mock private PhotoMstMapper photoMstMapper;

  @Mock private PhotoTagMstMapper photoTagMstMapper;

  @Mock private PhotoFavoriteMapper photoFavoriteMapper;

  @Mock private LocationMstMapper locationMstMapper;

  private PhotoDetailModel buildDetail(
      AccountNo accountNo, PhotoNo photoNo, ImageFilePath imageFilePath, PhotoTagModelList tags) {
    MultipartFile multipartFile =
        new MockMultipartFile(
            "file", "DSC111.jpg", "multipart/form-data", "sample image".getBytes());
    return PhotoDetailModel.builder()
        .accountNo(accountNo)
        .photoNo(photoNo)
        .imageFile(new ImageFile(multipartFile))
        .imageFilePath(imageFilePath)
        .photoTagModelList(tags)
        .build();
  }

  private PhotoTagModel buildTag(AccountNo accountNo, String japaneseName) {
    return PhotoTagModel.builder()
        .accountNo(accountNo)
        .tagJapaneseName(new TagJapaneseName(japaneseName))
        .tagEnglishName(new TagEnglishName(japaneseName))
        .build();
  }

  private PhotoDetailModel withLocation(
      PhotoDetailModel base,
      LocationNo locationNo,
      LocationManagementName managementName,
      LocationDisplayName displayName,
      GeoLocation geoLocation) {
    return base.toBuilder()
        .locationNo(locationNo)
        .managementName(managementName)
        .displayName(displayName)
        .geoLocation(geoLocation)
        .build();
  }

  private LocationMst buildLocationMst(
      Long accountNo, Long locationNo, String managementName, String displayName) {
    return LocationMst.builder()
        .accountNo(accountNo)
        .locationNo(locationNo)
        .managementName(managementName)
        .displayName(displayName)
        .address("東京都渋谷区")
        .latitude(new java.math.BigDecimal("35.6812"))
        .longitude(new java.math.BigDecimal("139.7671"))
        .build();
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class regist {
    @Test
    @Order(1)
    @DisplayName("正常系：重複がなければ写真マスタ・タグを登録すること")
    void regist_success() throws GalleryException {
      AccountNo accountNo = new AccountNo(1L);
      PhotoNo photoNo = new PhotoNo(5L);
      ImageFilePath imageFilePath = new ImageFilePath("/path/DSC111.jpg");
      PhotoTagModelList tags =
          PhotoTagModelList.of(List.of(buildTag(accountNo, "太陽"), buildTag(accountNo, "海")));
      PhotoDetailModel requestDetail = buildDetail(accountNo, null, new ImageFilePath(""), tags);
      Photo photo = Photo.forRegist(requestDetail, photoNo, imageFilePath);

      doReturn(false).when(photoMstMapper).isExistPhoto(any(PhotoMstCondition.class));

      ArgumentCaptor<PhotoMst> photoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
      doReturn(1).when(photoMstMapper).insert(photoMstCaptor.capture());

      @SuppressWarnings("unchecked")
      ArgumentCaptor<List<PhotoTagMst>> photoTagMstListCaptor = ArgumentCaptor.forClass(List.class);
      doReturn(2).when(photoTagMstMapper).insertBulk(photoTagMstListCaptor.capture());

      photoAggregateRepositoryImpl.regist(photo);

      InOrder inOrder = inOrder(photoMstMapper, photoTagMstMapper);
      // 重複チェックを先に行う
      inOrder.verify(photoMstMapper).isExistPhoto(any(PhotoMstCondition.class));
      // タグは写真マスタへの外部キー制約を持つため、先に写真マスタを登録する
      inOrder.verify(photoMstMapper).insert(any(PhotoMst.class));
      inOrder.verify(photoTagMstMapper).insertBulk(anyList());

      PhotoMst photoMstCapture = photoMstCaptor.getValue();
      assertEquals(accountNo.value(), photoMstCapture.getAccountNo());
      assertEquals(photoNo.value(), photoMstCapture.getPhotoNo());
      assertEquals(imageFilePath.value(), photoMstCapture.getImageFilePath());

      List<PhotoTagMst> photoTagMstCaptureList = photoTagMstListCaptor.getValue();
      assertEquals(2, photoTagMstCaptureList.size());
      assertEquals(1L, photoTagMstCaptureList.get(0).getTagNo());
      assertEquals(photoNo.value(), photoTagMstCaptureList.get(0).getPhotoNo());
      assertEquals(2L, photoTagMstCaptureList.get(1).getTagNo());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：同じファイル名が既に存在する場合、FileDuplicateExceptionをthrowすること")
    void regist_duplicate() throws GalleryException {
      AccountNo accountNo = new AccountNo(1L);
      PhotoDetailModel requestDetail =
          buildDetail(accountNo, null, new ImageFilePath(""), PhotoTagModelList.empty());
      Photo photo =
          Photo.forRegist(requestDetail, new PhotoNo(5L), new ImageFilePath("/path/DSC111.jpg"));

      doReturn(true).when(photoMstMapper).isExistPhoto(any(PhotoMstCondition.class));

      assertThrows(FileDuplicateException.class, () -> photoAggregateRepositoryImpl.regist(photo));

      verify(photoMstMapper, times(0)).insert(any(PhotoMst.class));
      verify(photoTagMstMapper, times(0)).insertBulk(anyList());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：写真マスタ登録でDuplicateKeyExceptionが発生した場合、RegistFailureExceptionをthrowすること")
    void regist_RegistFailureException() {
      AccountNo accountNo = new AccountNo(1L);
      PhotoDetailModel requestDetail =
          buildDetail(accountNo, null, new ImageFilePath(""), PhotoTagModelList.empty());
      Photo photo =
          Photo.forRegist(requestDetail, new PhotoNo(5L), new ImageFilePath("/path/DSC111.jpg"));

      doReturn(false).when(photoMstMapper).isExistPhoto(any(PhotoMstCondition.class));
      doThrow(DuplicateKeyException.class).when(photoMstMapper).insert(any(PhotoMst.class));

      assertThrows(RegistFailureException.class, () -> photoAggregateRepositoryImpl.regist(photo));

      verify(photoTagMstMapper, times(0)).insertBulk(anyList());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：写真タグ登録でDuplicateKeyExceptionが発生した場合、RegistFailureExceptionをthrowすること")
    void regist_tag_RegistFailureException() {
      AccountNo accountNo = new AccountNo(1L);
      PhotoTagModelList tags = PhotoTagModelList.of(List.of(buildTag(accountNo, "太陽")));
      PhotoDetailModel requestDetail = buildDetail(accountNo, null, new ImageFilePath(""), tags);
      Photo photo =
          Photo.forRegist(requestDetail, new PhotoNo(5L), new ImageFilePath("/path/DSC111.jpg"));

      doReturn(false).when(photoMstMapper).isExistPhoto(any(PhotoMstCondition.class));
      doReturn(1).when(photoMstMapper).insert(any(PhotoMst.class));
      doThrow(DuplicateKeyException.class).when(photoTagMstMapper).insertBulk(anyList());

      assertThrows(RegistFailureException.class, () -> photoAggregateRepositoryImpl.regist(photo));

      verify(photoMstMapper).insert(any(PhotoMst.class));
      verify(photoTagMstMapper).insertBulk(anyList());
    }

    @Test
    @Order(5)
    @DisplayName("正常系：既存ロケーション番号が本人所有の場合、そのロケーション番号を採用すること")
    void regist_location_existingSelection() throws GalleryException {
      AccountNo accountNo = new AccountNo(1L);
      PhotoDetailModel requestDetail =
          withLocation(
              buildDetail(accountNo, null, new ImageFilePath(""), PhotoTagModelList.empty()),
              new LocationNo(3L),
              null,
              null,
              null);
      Photo photo =
          Photo.forRegist(requestDetail, new PhotoNo(5L), new ImageFilePath("/path/DSC111.jpg"));

      doReturn(false).when(photoMstMapper).isExistPhoto(any(PhotoMstCondition.class));
      doReturn(List.of(buildLocationMst(1L, 3L, "渋谷_管理用", "渋谷")))
          .when(locationMstMapper)
          .select(any(LocationMstCondition.class));

      ArgumentCaptor<PhotoMst> photoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
      doReturn(1).when(photoMstMapper).insert(photoMstCaptor.capture());

      photoAggregateRepositoryImpl.regist(photo);

      assertEquals(3L, photoMstCaptor.getValue().getLocationNo());
      verify(locationMstMapper, times(0)).insert(any(LocationMst.class));
    }

    @Test
    @Order(6)
    @DisplayName("異常系：既存ロケーション番号が本人所有でない場合、BadRequestExceptionをthrowすること")
    void regist_location_notOwned() {
      AccountNo accountNo = new AccountNo(1L);
      PhotoDetailModel requestDetail =
          withLocation(
              buildDetail(accountNo, null, new ImageFilePath(""), PhotoTagModelList.empty()),
              new LocationNo(3L),
              null,
              null,
              null);
      Photo photo =
          Photo.forRegist(requestDetail, new PhotoNo(5L), new ImageFilePath("/path/DSC111.jpg"));

      doReturn(false).when(photoMstMapper).isExistPhoto(any(PhotoMstCondition.class));
      doReturn(List.of()).when(locationMstMapper).select(any(LocationMstCondition.class));

      assertThrows(BadRequestException.class, () -> photoAggregateRepositoryImpl.regist(photo));

      verify(photoMstMapper, times(0)).insert(any(PhotoMst.class));
    }

    @Test
    @Order(7)
    @DisplayName("正常系：新規入力のロケーション名が既存マスタと同名の場合、既存のロケーション番号を再利用すること")
    void regist_location_reuseExistingByName() throws GalleryException {
      AccountNo accountNo = new AccountNo(1L);
      GeoLocation geoLocation =
          new GeoLocation(
              new Address("東京都渋谷区"),
              new Latitude(new java.math.BigDecimal("35.6812")),
              new Longitude(new java.math.BigDecimal("139.7671")));
      PhotoDetailModel requestDetail =
          withLocation(
              buildDetail(accountNo, null, new ImageFilePath(""), PhotoTagModelList.empty()),
              null,
              new LocationManagementName("渋谷スクランブル交差点_管理用"),
              new LocationDisplayName("渋谷スクランブル交差点"),
              geoLocation);
      Photo photo =
          Photo.forRegist(requestDetail, new PhotoNo(5L), new ImageFilePath("/path/DSC111.jpg"));

      doReturn(false).when(photoMstMapper).isExistPhoto(any(PhotoMstCondition.class));
      doReturn(List.of(buildLocationMst(1L, 7L, "渋谷スクランブル交差点_管理用", "渋谷スクランブル交差点")))
          .when(locationMstMapper)
          .select(any(LocationMstCondition.class));

      ArgumentCaptor<PhotoMst> photoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
      doReturn(1).when(photoMstMapper).insert(photoMstCaptor.capture());

      photoAggregateRepositoryImpl.regist(photo);

      assertEquals(7L, photoMstCaptor.getValue().getLocationNo());
      verify(locationMstMapper, times(0)).insert(any(LocationMst.class));
    }

    @Test
    @Order(8)
    @DisplayName("正常系：新規入力のロケーション名が既存マスタと同名でない場合、新規採番してロケーションマスタへ登録すること")
    void regist_location_registNew() throws GalleryException {
      AccountNo accountNo = new AccountNo(1L);
      GeoLocation geoLocation =
          new GeoLocation(
              new Address("東京都渋谷区"),
              new Latitude(new java.math.BigDecimal("35.6812")),
              new Longitude(new java.math.BigDecimal("139.7671")));
      PhotoDetailModel requestDetail =
          withLocation(
              buildDetail(accountNo, null, new ImageFilePath(""), PhotoTagModelList.empty()),
              null,
              new LocationManagementName("渋谷スクランブル交差点_管理用"),
              new LocationDisplayName("渋谷スクランブル交差点"),
              geoLocation);
      Photo photo =
          Photo.forRegist(requestDetail, new PhotoNo(5L), new ImageFilePath("/path/DSC111.jpg"));

      doReturn(false).when(photoMstMapper).isExistPhoto(any(PhotoMstCondition.class));
      doReturn(List.of()).when(locationMstMapper).select(any(LocationMstCondition.class));
      doReturn(null).when(locationMstMapper).getMaxLocationNo(accountNo.value());

      ArgumentCaptor<LocationMst> locationMstCaptor = ArgumentCaptor.forClass(LocationMst.class);
      doReturn(1).when(locationMstMapper).insert(locationMstCaptor.capture());

      ArgumentCaptor<PhotoMst> photoMstCaptor = ArgumentCaptor.forClass(PhotoMst.class);
      doReturn(1).when(photoMstMapper).insert(photoMstCaptor.capture());

      photoAggregateRepositoryImpl.regist(photo);

      LocationMst capturedLocationMst = locationMstCaptor.getValue();
      assertEquals(accountNo.value(), capturedLocationMst.getAccountNo());
      assertEquals(1L, capturedLocationMst.getLocationNo());
      assertEquals(accountNo.value(), capturedLocationMst.getCreatedBy());
      assertEquals("渋谷スクランブル交差点_管理用", capturedLocationMst.getManagementName());
      assertEquals("渋谷スクランブル交差点", capturedLocationMst.getDisplayName());
      assertEquals("東京都渋谷区", capturedLocationMst.getAddress());
      assertEquals(new java.math.BigDecimal("35.6812"), capturedLocationMst.getLatitude());
      assertEquals(new java.math.BigDecimal("139.7671"), capturedLocationMst.getLongitude());

      assertEquals(1L, photoMstCaptor.getValue().getLocationNo());
    }

    @Test
    @Order(9)
    @DisplayName("異常系：新規ロケーション登録でDuplicateKeyExceptionが発生した場合、RegistFailureExceptionをthrowすること")
    void regist_location_RegistFailureException() {
      AccountNo accountNo = new AccountNo(1L);
      GeoLocation geoLocation =
          new GeoLocation(
              new Address("東京都渋谷区"),
              new Latitude(new java.math.BigDecimal("35.6812")),
              new Longitude(new java.math.BigDecimal("139.7671")));
      PhotoDetailModel requestDetail =
          withLocation(
              buildDetail(accountNo, null, new ImageFilePath(""), PhotoTagModelList.empty()),
              null,
              new LocationManagementName("渋谷スクランブル交差点_管理用"),
              new LocationDisplayName("渋谷スクランブル交差点"),
              geoLocation);
      Photo photo =
          Photo.forRegist(requestDetail, new PhotoNo(5L), new ImageFilePath("/path/DSC111.jpg"));

      doReturn(false).when(photoMstMapper).isExistPhoto(any(PhotoMstCondition.class));
      doReturn(List.of()).when(locationMstMapper).select(any(LocationMstCondition.class));
      doReturn(5L).when(locationMstMapper).getMaxLocationNo(accountNo.value());
      doThrow(DuplicateKeyException.class).when(locationMstMapper).insert(any(LocationMst.class));

      assertThrows(RegistFailureException.class, () -> photoAggregateRepositoryImpl.regist(photo));

      verify(photoMstMapper, times(0)).insert(any(PhotoMst.class));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class update {
    @Test
    @Order(1)
    @DisplayName("正常系：写真マスタを更新し、タグを全削除してから再登録すること")
    void update_success() throws GalleryException {
      AccountNo accountNo = new AccountNo(1L);
      PhotoNo photoNo = new PhotoNo(5L);
      PhotoTagModelList tags = PhotoTagModelList.of(List.of(buildTag(accountNo, "太陽")));
      PhotoDetailModel requestDetail =
          buildDetail(accountNo, photoNo, new ImageFilePath("/path/DSC111.jpg"), tags);
      Photo photo = Photo.forUpdate(requestDetail);

      ArgumentCaptor<PhotoMstCondition> conditionCaptor =
          ArgumentCaptor.forClass(PhotoMstCondition.class);
      ArgumentCaptor<PhotoMstUpdateTarget> targetCaptor =
          ArgumentCaptor.forClass(PhotoMstUpdateTarget.class);
      doReturn(1).when(photoMstMapper).update(conditionCaptor.capture(), targetCaptor.capture());

      ArgumentCaptor<PhotoTagMstCondition> tagConditionCaptor =
          ArgumentCaptor.forClass(PhotoTagMstCondition.class);
      doReturn(1).when(photoTagMstMapper).delete(tagConditionCaptor.capture());

      @SuppressWarnings("unchecked")
      ArgumentCaptor<List<PhotoTagMst>> photoTagMstListCaptor = ArgumentCaptor.forClass(List.class);
      doReturn(1).when(photoTagMstMapper).insertBulk(photoTagMstListCaptor.capture());

      photoAggregateRepositoryImpl.update(photo);

      InOrder inOrder = inOrder(photoMstMapper, photoTagMstMapper);
      inOrder
          .verify(photoMstMapper)
          .update(any(PhotoMstCondition.class), any(PhotoMstUpdateTarget.class));
      // 既存タグを全削除してから
      inOrder.verify(photoTagMstMapper).delete(any(PhotoTagMstCondition.class));
      // 新しいタグを再登録する
      inOrder.verify(photoTagMstMapper).insertBulk(anyList());

      assertEquals(accountNo.value(), conditionCaptor.getValue().getAccountNo());
      assertEquals(photoNo.value(), conditionCaptor.getValue().getPhotoNo());
      assertFalse(conditionCaptor.getValue().getIsDeleted());

      assertEquals(accountNo.value(), tagConditionCaptor.getValue().getAccountNo());
      assertEquals(photoNo.value(), tagConditionCaptor.getValue().getPhotoNo());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：写真マスタの更新件数が0件の場合、UpdateFailureExceptionをthrowすること")
    void update_UpdateFailureException() {
      AccountNo accountNo = new AccountNo(1L);
      PhotoNo photoNo = new PhotoNo(5L);
      PhotoDetailModel requestDetail =
          buildDetail(
              accountNo, photoNo, new ImageFilePath("/path/DSC111.jpg"), PhotoTagModelList.empty());
      Photo photo = Photo.forUpdate(requestDetail);

      doReturn(0)
          .when(photoMstMapper)
          .update(any(PhotoMstCondition.class), any(PhotoMstUpdateTarget.class));

      assertThrows(UpdateFailureException.class, () -> photoAggregateRepositoryImpl.update(photo));

      verify(photoTagMstMapper, times(0)).delete(any(PhotoTagMstCondition.class));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class delete {
    @Test
    @Order(1)
    @DisplayName("正常系：お気に入り・タグを削除してから写真マスタを論理削除すること")
    void delete_success() throws GalleryException {
      AccountNo accountNo = new AccountNo(1L);
      PhotoNo photoNo = new PhotoNo(5L);
      ImageFilePath imageFilePathForDelete = new ImageFilePath("/path/DSC111.jpg");
      Photo photo = Photo.forDelete(accountNo, photoNo, imageFilePathForDelete);

      ArgumentCaptor<PhotoFavoriteCondition> favoriteConditionCaptor =
          ArgumentCaptor.forClass(PhotoFavoriteCondition.class);
      doReturn(1).when(photoFavoriteMapper).delete(favoriteConditionCaptor.capture());

      ArgumentCaptor<PhotoTagMstCondition> tagConditionCaptor =
          ArgumentCaptor.forClass(PhotoTagMstCondition.class);
      doReturn(1).when(photoTagMstMapper).delete(tagConditionCaptor.capture());

      ArgumentCaptor<PhotoMstCondition> conditionCaptor =
          ArgumentCaptor.forClass(PhotoMstCondition.class);
      ArgumentCaptor<PhotoMstUpdateTarget> targetCaptor =
          ArgumentCaptor.forClass(PhotoMstUpdateTarget.class);
      doReturn(1).when(photoMstMapper).update(conditionCaptor.capture(), targetCaptor.capture());

      photoAggregateRepositoryImpl.delete(photo);

      InOrder inOrder = inOrder(photoFavoriteMapper, photoTagMstMapper, photoMstMapper);
      // 外部キー制約に抵触しないよう、お気に入りを先に削除する
      inOrder.verify(photoFavoriteMapper).delete(any(PhotoFavoriteCondition.class));
      // 次にタグを削除する
      inOrder.verify(photoTagMstMapper).delete(any(PhotoTagMstCondition.class));
      // 最後に写真マスタを論理削除する
      inOrder
          .verify(photoMstMapper)
          .update(any(PhotoMstCondition.class), any(PhotoMstUpdateTarget.class));

      assertNull(favoriteConditionCaptor.getValue().getAccountNo());
      assertEquals(
          accountNo.value(), favoriteConditionCaptor.getValue().getFavoritePhotoAccountNo());
      assertEquals(photoNo.value(), favoriteConditionCaptor.getValue().getFavoritePhotoNo());

      assertEquals(accountNo.value(), tagConditionCaptor.getValue().getAccountNo());
      assertEquals(photoNo.value(), tagConditionCaptor.getValue().getPhotoNo());

      assertEquals(accountNo.value(), conditionCaptor.getValue().getAccountNo());
      assertEquals(photoNo.value(), conditionCaptor.getValue().getPhotoNo());
      assertFalse(conditionCaptor.getValue().getIsDeleted());
      assertTrue(targetCaptor.getValue().getIsDeleted());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：写真マスタの更新件数が0件（対象写真が存在しない）の場合、PhotoNotFoundExceptionをthrowすること")
    void delete_PhotoNotFoundException() {
      AccountNo accountNo = new AccountNo(1L);
      PhotoNo photoNo = new PhotoNo(5L);
      Photo photo = Photo.forDelete(accountNo, photoNo, new ImageFilePath("/path/DSC111.jpg"));

      doReturn(1).when(photoFavoriteMapper).delete(any(PhotoFavoriteCondition.class));
      doReturn(1).when(photoTagMstMapper).delete(any(PhotoTagMstCondition.class));
      doReturn(0)
          .when(photoMstMapper)
          .update(any(PhotoMstCondition.class), any(PhotoMstUpdateTarget.class));

      assertThrows(PhotoNotFoundException.class, () -> photoAggregateRepositoryImpl.delete(photo));
    }
  }
}
