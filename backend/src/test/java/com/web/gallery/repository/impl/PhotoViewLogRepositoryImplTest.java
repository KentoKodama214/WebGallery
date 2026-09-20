package com.web.gallery.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.Country;
import com.web.gallery.domain.common.IpAddress;
import com.web.gallery.domain.common.IpGeoLocation;
import com.web.gallery.domain.common.Referer;
import com.web.gallery.domain.common.Region;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.entity.photo.PhotoViewLog;
import com.web.gallery.entity.photo.PhotoViewLogCondition;
import com.web.gallery.mapper.PhotoViewLogMapper;
import com.web.gallery.model.photo.PhotoViewLogModel;
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
public class PhotoViewLogRepositoryImplTest {
  @InjectMocks private PhotoViewLogRepositoryImpl photoViewLogRepositoryImpl;

  @Mock private PhotoViewLogMapper photoViewLogMapper;

  private PhotoViewLogModel.PhotoViewLogModelBuilder requiredFieldsBuilder() {
    return PhotoViewLogModel.builder()
        .photoAccountNo(new AccountNo(1L))
        .photoNo(new PhotoNo(1L))
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
      PhotoViewLogModel model = requiredFieldsBuilder().build();

      ArgumentCaptor<PhotoViewLog> captor = ArgumentCaptor.forClass(PhotoViewLog.class);
      doReturn(1).when(photoViewLogMapper).insert(captor.capture());

      photoViewLogRepositoryImpl.save(model);

      PhotoViewLog actual = captor.getValue();
      assertEquals(1L, actual.getPhotoAccountNo());
      assertEquals(0L, actual.getAccountNo());
      assertEquals(1L, actual.getPhotoNo());
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
      PhotoViewLogModel model =
          requiredFieldsBuilder()
              .accountNo(new AccountNo(2L))
              .referer(new Referer("https://example.com/"))
              .geoLocation(new IpGeoLocation(new Country("JP"), new Region("Tokyo")))
              .build();

      ArgumentCaptor<PhotoViewLog> captor = ArgumentCaptor.forClass(PhotoViewLog.class);
      doReturn(1).when(photoViewLogMapper).insert(captor.capture());

      photoViewLogRepositoryImpl.save(model);

      PhotoViewLog actual = captor.getValue();
      assertEquals(2L, actual.getAccountNo());
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
      ArgumentCaptor<PhotoViewLogCondition> captor =
          ArgumentCaptor.forClass(PhotoViewLogCondition.class);
      doReturn(1).when(photoViewLogMapper).delete(captor.capture());

      photoViewLogRepositoryImpl.deleteByPhotoAccountNo(new AccountNo(1L));

      assertEquals(1L, captor.getValue().getPhotoAccountNo());
    }
  }
}
