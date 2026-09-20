package com.web.gallery.entity;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.Country;
import com.web.gallery.domain.common.IpAddress;
import com.web.gallery.domain.common.IpGeoLocation;
import com.web.gallery.domain.common.Region;
import com.web.gallery.model.LoginHistoryModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class LoginHistoryTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、そのまま値が反映されること")
    void from_allFieldsPresent() {
      LoginHistoryModel model =
          LoginHistoryModel.of(
              new AccountNo(1L),
              new IpAddress("203.0.113.1"),
              new IpGeoLocation(new Country("JP"), new Region("Tokyo")));

      LoginHistory actual = LoginHistory.from(model);

      assertEquals(1L, actual.getAccountNo());
      assertEquals("203.0.113.1", actual.getIpAddress());
      assertEquals("JP", actual.getCountry());
      assertEquals("Tokyo", actual.getRegion());
      assertEquals(1L, actual.getCreatedBy());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：国・地域が未解決の場合、それぞれ空文字が反映されること")
    void from_geoLocationUnresolved() {
      LoginHistoryModel model =
          LoginHistoryModel.of(
              new AccountNo(1L), new IpAddress("203.0.113.1"), IpGeoLocation.empty());

      LoginHistory actual = LoginHistory.from(model);

      assertEquals("", actual.getCountry());
      assertEquals("", actual.getRegion());
    }
  }
}
