package com.web.gallery.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.Country;
import com.web.gallery.domain.common.IpAddress;
import com.web.gallery.domain.common.IpGeoLocation;
import com.web.gallery.domain.common.Referer;
import com.web.gallery.domain.common.Region;
import com.web.gallery.domain.photo.IsFavoriteOnly;
import com.web.gallery.entity.PhotoListFilterLog;
import com.web.gallery.entity.PhotoListFilterLogCondition;
import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.enumeration.SortPhotoEnum;
import com.web.gallery.mapper.PhotoListFilterLogMapper;
import com.web.gallery.model.PhotoListFilterLogModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PhotoListFilterLogRepositoryImplTest {
  @InjectMocks private PhotoListFilterLogRepositoryImpl photoListFilterLogRepositoryImpl;

  @Mock private PhotoListFilterLogMapper photoListFilterLogMapper;

  private PhotoListFilterLogModel.PhotoListFilterLogModelBuilder requiredFieldsBuilder() {
    return PhotoListFilterLogModel.builder()
        .photoAccountNo(new AccountNo(1L))
        .directionKbn(DirectionEnum.HORIZONTAL)
        .isFavoriteOnly(new IsFavoriteOnly(false))
        .tagList("")
        .sortBy(SortPhotoEnum.PHOTO_AT)
        .referer(new Referer(""))
        .ipAddress(new IpAddress("127.0.0.1"))
        .geoLocation(IpGeoLocation.empty());
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class save {
    @Test
    @Order(1)
    @DisplayName("正常系：必須項目のみ指定した場合、未ログイン閲覧者アカウント番号は0、国・地域は空文字へ変換して登録すること")
    void save_requiredFieldsOnly() {
      PhotoListFilterLogModel model = requiredFieldsBuilder().build();

      ArgumentCaptor<PhotoListFilterLog> captor = ArgumentCaptor.forClass(PhotoListFilterLog.class);
      doReturn(1).when(photoListFilterLogMapper).insert(captor.capture());

      photoListFilterLogRepositoryImpl.save(model);

      PhotoListFilterLog actual = captor.getValue();
      assertEquals(1L, actual.getPhotoAccountNo());
      assertEquals(0L, actual.getAccountNo());
      assertEquals(DirectionEnum.HORIZONTAL, actual.getDirectionKbn());
      assertFalse(actual.getIsFavorite());
      assertEquals("", actual.getTagList());
      assertEquals(SortPhotoEnum.PHOTO_AT, actual.getSortBy());
      assertEquals("", actual.getReferer());
      assertEquals("127.0.0.1", actual.getIpAddress());
      assertEquals("", actual.getCountry());
      assertEquals("", actual.getRegion());
      assertEquals(1L, actual.getCreatedBy());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：全項目指定した場合、そのままEntityへ変換して登録すること")
    void save_allFieldsSet() {
      PhotoListFilterLogModel model =
          requiredFieldsBuilder()
              .accountNo(new AccountNo(2L))
              .isFavoriteOnly(new IsFavoriteOnly(true))
              .tagList("風景,夜景")
              .sortBy(SortPhotoEnum.FAVORITE)
              .referer(new Referer("https://example.com/"))
              .geoLocation(new IpGeoLocation(new Country("JP"), new Region("Tokyo")))
              .build();

      ArgumentCaptor<PhotoListFilterLog> captor = ArgumentCaptor.forClass(PhotoListFilterLog.class);
      doReturn(1).when(photoListFilterLogMapper).insert(captor.capture());

      photoListFilterLogRepositoryImpl.save(model);

      PhotoListFilterLog actual = captor.getValue();
      assertEquals(2L, actual.getAccountNo());
      assertTrue(actual.getIsFavorite());
      assertEquals("風景,夜景", actual.getTagList());
      assertEquals(SortPhotoEnum.FAVORITE, actual.getSortBy());
      assertEquals("https://example.com/", actual.getReferer());
      assertEquals("JP", actual.getCountry());
      assertEquals("Tokyo", actual.getRegion());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class deleteByPhotoAccountNo {
    @Test
    @Order(1)
    @DisplayName("正常系：指定した写真アカウント番号の抽出条件でMapperへ削除を委譲すること")
    void deleteByPhotoAccountNo_success() {
      ArgumentCaptor<PhotoListFilterLogCondition> captor =
          ArgumentCaptor.forClass(PhotoListFilterLogCondition.class);
      doReturn(1).when(photoListFilterLogMapper).delete(captor.capture());

      photoListFilterLogRepositoryImpl.deleteByPhotoAccountNo(new AccountNo(1L));

      assertEquals(1L, captor.getValue().getPhotoAccountNo());
    }
  }
}
