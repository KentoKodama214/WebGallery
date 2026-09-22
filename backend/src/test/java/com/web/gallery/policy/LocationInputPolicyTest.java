package com.web.gallery.policy;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.common.Address;
import com.web.gallery.domain.common.GeoLocation;
import com.web.gallery.domain.common.Latitude;
import com.web.gallery.domain.common.LocationName;
import com.web.gallery.domain.common.Longitude;
import com.web.gallery.domain.photo.LocationNo;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class LocationInputPolicyTest {

  private final LocationInputPolicy locationInputPolicy = new LocationInputPolicy();

  private GeoLocation geoLocationWithCoordinates() {
    return new GeoLocation(
        new Address("東京都渋谷区"),
        new Latitude(new BigDecimal("35.6812")),
        new Longitude(new BigDecimal("139.7671")));
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class isValid {
    @Test
    @Order(1)
    @DisplayName("正常系：ロケーション番号（既存選択）が指定されている場合、名称・緯度経度の有無を問わずtrueを返すこと")
    void isValid_existingSelection() {
      assertTrue(locationInputPolicy.isValid(new LocationNo(3L), null, GeoLocation.empty()));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：ロケーション情報が何も指定されていない場合、trueを返すこと")
    void isValid_notSet() {
      assertTrue(locationInputPolicy.isValid(null, null, GeoLocation.empty()));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：新規入力でロケーション名・緯度・経度が揃っている場合、trueを返すこと")
    void isValid_newInput_complete() {
      assertTrue(
          locationInputPolicy.isValid(
              null, new LocationName("渋谷スクランブル交差点"), geoLocationWithCoordinates()));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：新規入力でロケーション名は指定されているが緯度・経度が未指定の場合、falseを返すこと")
    void isValid_newInput_missingCoordinates() {
      assertFalse(
          locationInputPolicy.isValid(null, new LocationName("渋谷スクランブル交差点"), GeoLocation.empty()));
    }

    @Test
    @Order(5)
    @DisplayName("異常系：新規入力で緯度のみ未指定の場合、falseを返すこと")
    void isValid_newInput_missingLatitude() {
      GeoLocation geoLocation =
          new GeoLocation(new Address("東京都渋谷区"), null, new Longitude(new BigDecimal("139.7671")));
      assertFalse(locationInputPolicy.isValid(null, new LocationName("渋谷スクランブル交差点"), geoLocation));
    }

    @Test
    @Order(6)
    @DisplayName("異常系：新規入力で経度のみ未指定の場合、falseを返すこと")
    void isValid_newInput_missingLongitude() {
      GeoLocation geoLocation =
          new GeoLocation(new Address("東京都渋谷区"), new Latitude(new BigDecimal("35.6812")), null);
      assertFalse(locationInputPolicy.isValid(null, new LocationName("渋谷スクランブル交差点"), geoLocation));
    }

    @Test
    @Order(7)
    @DisplayName("正常系：ロケーション番号が0（未設定センチネル）の場合は新規入力の判定に進むこと")
    void isValid_locationNoZero_fallsThroughToNewInputCheck() {
      assertFalse(
          locationInputPolicy.isValid(
              new LocationNo(0L), new LocationName("渋谷スクランブル交差点"), GeoLocation.empty()));
    }
  }
}
