package com.web.gallery.entity.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.Country;
import com.web.gallery.domain.common.IpAddress;
import com.web.gallery.domain.common.IpGeoLocation;
import com.web.gallery.domain.common.Referer;
import com.web.gallery.domain.common.Region;
import com.web.gallery.domain.photo.IsFavoriteOnly;
import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.enumeration.SortPhotoEnum;
import com.web.gallery.model.photo.PhotoListFilterLogModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PhotoListFilterLogTest {

  private PhotoListFilterLogModel.PhotoListFilterLogModelBuilder baseBuilder() {
    return PhotoListFilterLogModel.builder()
        .photoAccountNo(new AccountNo(1L))
        .directionKbn(DirectionEnum.VERTICAL)
        .isFavoriteOnly(new IsFavoriteOnly(true))
        .tagList("風景,海")
        .sortBy(SortPhotoEnum.FAVORITE)
        .referer(new Referer("https://example.com/"))
        .ipAddress(new IpAddress("203.0.113.1"))
        .geoLocation(new IpGeoLocation(new Country("JP"), new Region("Tokyo")));
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、そのまま値が反映されること")
    void from_allFieldsPresent() {
      PhotoListFilterLogModel model = baseBuilder().accountNo(new AccountNo(2L)).build();

      PhotoListFilterLog actual = PhotoListFilterLog.from(model);

      assertEquals(1L, actual.getPhotoAccountNo());
      assertEquals(2L, actual.getAccountNo());
      assertEquals(DirectionEnum.VERTICAL, actual.getDirectionKbn());
      assertTrue(actual.getIsFavorite());
      assertEquals("風景,海", actual.getTagList());
      assertEquals(SortPhotoEnum.FAVORITE, actual.getSortBy());
      assertEquals("https://example.com/", actual.getReferer());
      assertEquals("203.0.113.1", actual.getIpAddress());
      assertEquals("JP", actual.getCountry());
      assertEquals("Tokyo", actual.getRegion());
      assertEquals(1L, actual.getCreatedBy());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：閲覧者のアカウント番号が未ログインでnullの場合、0が反映されること")
    void from_accountNoNull() {
      PhotoListFilterLogModel model = baseBuilder().accountNo(null).build();

      PhotoListFilterLog actual = PhotoListFilterLog.from(model);

      assertEquals(0L, actual.getAccountNo());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：国・地域が未解決の場合、それぞれ空文字が反映されること")
    void from_geoLocationUnresolved() {
      PhotoListFilterLogModel model =
          baseBuilder().accountNo(new AccountNo(2L)).geoLocation(IpGeoLocation.empty()).build();

      PhotoListFilterLog actual = PhotoListFilterLog.from(model);

      assertEquals("", actual.getCountry());
      assertEquals("", actual.getRegion());
    }
  }
}
