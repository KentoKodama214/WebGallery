package com.web.gallery.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.Country;
import com.web.gallery.domain.common.IpAddress;
import com.web.gallery.domain.common.IpGeoLocation;
import com.web.gallery.domain.common.Region;
import com.web.gallery.entity.LoginHistory;
import com.web.gallery.entity.LoginHistoryCondition;
import com.web.gallery.mapper.LoginHistoryMapper;
import com.web.gallery.model.LoginHistoryModel;
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
public class LoginHistoryRepositoryImplTest {
  @InjectMocks private LoginHistoryRepositoryImpl loginHistoryRepositoryImpl;

  @Mock private LoginHistoryMapper loginHistoryMapper;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class save {
    @Test
    @Order(1)
    @DisplayName("正常系：国・地域が解決できた場合、そのままEntityへ変換して登録すること")
    void save_geoLocationResolved() {
      LoginHistoryModel model =
          LoginHistoryModel.of(
              new AccountNo(1L),
              new IpAddress("127.0.0.1"),
              new IpGeoLocation(new Country("JP"), new Region("Tokyo")));

      ArgumentCaptor<LoginHistory> captor = ArgumentCaptor.forClass(LoginHistory.class);
      doReturn(1).when(loginHistoryMapper).insert(captor.capture());

      loginHistoryRepositoryImpl.save(model);

      LoginHistory actual = captor.getValue();
      assertEquals(1L, actual.getAccountNo());
      assertEquals("127.0.0.1", actual.getIpAddress());
      assertEquals("JP", actual.getCountry());
      assertEquals("Tokyo", actual.getRegion());
      assertEquals(1L, actual.getCreatedBy());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：国・地域が未解決の場合、それぞれ空文字へ変換して登録すること")
    void save_geoLocationUnresolved() {
      LoginHistoryModel model =
          LoginHistoryModel.of(
              new AccountNo(1L), new IpAddress("127.0.0.1"), IpGeoLocation.empty());

      ArgumentCaptor<LoginHistory> captor = ArgumentCaptor.forClass(LoginHistory.class);
      doReturn(1).when(loginHistoryMapper).insert(captor.capture());

      loginHistoryRepositoryImpl.save(model);

      LoginHistory actual = captor.getValue();
      assertEquals("", actual.getCountry());
      assertEquals("", actual.getRegion());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class deleteByAccountNo {
    @Test
    @Order(1)
    @DisplayName("正常系：指定したアカウント番号の抽出条件でMapperへ削除を委譲すること")
    void deleteByAccountNo_success() {
      ArgumentCaptor<LoginHistoryCondition> captor =
          ArgumentCaptor.forClass(LoginHistoryCondition.class);
      doReturn(1).when(loginHistoryMapper).delete(captor.capture());

      loginHistoryRepositoryImpl.deleteByAccountNo(new AccountNo(1L));

      assertEquals(1L, captor.getValue().getAccountNo());
    }
  }
}
