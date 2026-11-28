package com.web.gallery.presentation.controller.common;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.web.gallery.application.model.common.LocationModel;
import com.web.gallery.application.model.common.LocationModelList;
import com.web.gallery.application.service.common.LocationService;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.common.Address;
import com.web.gallery.domain.model.common.GeoLocation;
import com.web.gallery.domain.model.common.Latitude;
import com.web.gallery.domain.model.common.LocationDisplayName;
import com.web.gallery.domain.model.common.LocationManagementName;
import com.web.gallery.domain.model.common.Longitude;
import com.web.gallery.domain.model.photo.LocationNo;
import com.web.gallery.infrastructure.security.SessionHelper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class LocationControllerTest {
  @InjectMocks private LocationController locationController;

  @Mock private LocationService locationService;

  @Mock private SessionHelper sessionHelper;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(locationController)
            .setControllerAdvice(new CommonControllerAdvice())
            .build();
  }

  private LocationModel buildLocationModel(
      Long locationNo, String managementName, String displayName) {
    return LocationModel.builder()
        .accountNo(new AccountNo(1L))
        .locationNo(new LocationNo(locationNo))
        .managementName(new LocationManagementName(managementName))
        .displayName(new LocationDisplayName(displayName))
        .geoLocation(
            new GeoLocation(
                new Address("東京都渋谷区"),
                new Latitude(new BigDecimal("35.6812")),
                new Longitude(new BigDecimal("139.7671"))))
        .build();
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getLocationList {
    @Test
    @Order(1)
    @DisplayName("正常系：自分のアカウントの場合、ロケーション一覧を返すこと")
    void getLocationList_own_account() throws Exception {
      doReturn("aaaaaaaa").when(sessionHelper).getAccountId();
      doReturn(1L).when(sessionHelper).getAccountNo();
      LocationModelList locationModelList =
          LocationModelList.of(List.of(buildLocationModel(3L, "渋谷スクランブル交差点_管理用", "渋谷スクランブル交差点")));
      doReturn(locationModelList).when(locationService).getLocationList(new AccountNo(1L));

      mockMvc
          .perform(get("/api/v1/accounts/aaaaaaaa/locations"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.locations[0].locationNo").value(3))
          .andExpect(jsonPath("$.locations[0].managementName").value("渋谷スクランブル交差点_管理用"))
          .andExpect(jsonPath("$.locations[0].displayName").value("渋谷スクランブル交差点"))
          .andExpect(jsonPath("$.locations[0].address").value("東京都渋谷区"))
          .andExpect(jsonPath("$.locations[0].latitude").value(35.6812))
          .andExpect(jsonPath("$.locations[0].longitude").value(139.7671));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：他人のアカウントの場合、空のリストを返すこと")
    void getLocationList_other_account() throws Exception {
      doReturn("bbbbbbbb").when(sessionHelper).getAccountId();

      mockMvc
          .perform(get("/api/v1/accounts/aaaaaaaa/locations"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.locations").isArray())
          .andExpect(jsonPath("$.locations").isEmpty());

      verify(locationService, times(0)).getLocationList(any(AccountNo.class));
    }
  }
}
