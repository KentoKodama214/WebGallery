package com.web.gallery.entity;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.Country;
import com.web.gallery.domain.common.IpAddress;
import com.web.gallery.domain.common.IpGeoLocation;
import com.web.gallery.domain.common.Referer;
import com.web.gallery.domain.common.Region;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.model.PhotoViewLogModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PhotoViewLogTest {

  private PhotoViewLogModel.PhotoViewLogModelBuilder baseBuilder() {
    return PhotoViewLogModel.builder()
        .photoAccountNo(new AccountNo(1L))
        .photoNo(new PhotoNo(3L))
        .referer(new Referer("https://example.com/"))
        .ipAddress(new IpAddress("203.0.113.1"));
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、そのまま値が反映されること")
    void from_allFieldsPresent() {
      PhotoViewLogModel model =
          baseBuilder()
              .accountNo(new AccountNo(2L))
              .geoLocation(new IpGeoLocation(new Country("Japan"), new Region("Tokyo")))
              .build();

      PhotoViewLog actual = PhotoViewLog.from(model);

      assertEquals(1L, actual.getPhotoAccountNo());
      assertEquals(2L, actual.getAccountNo());
      assertEquals(3L, actual.getPhotoNo());
      assertEquals("https://example.com/", actual.getReferer());
      assertEquals("203.0.113.1", actual.getIpAddress());
      assertEquals("Japan", actual.getCountry());
      assertEquals("Tokyo", actual.getRegion());
      assertEquals(1L, actual.getCreatedBy());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：閲覧者のアカウント番号・国・地域が未設定の場合、デフォルト値が反映されること")
    void from_optionalFieldsAbsent() {
      PhotoViewLogModel model =
          baseBuilder().accountNo(null).geoLocation(IpGeoLocation.empty()).build();

      PhotoViewLog actual = PhotoViewLog.from(model);

      assertEquals(0L, actual.getAccountNo());
      assertEquals("", actual.getCountry());
      assertEquals("", actual.getRegion());
    }
  }
}
