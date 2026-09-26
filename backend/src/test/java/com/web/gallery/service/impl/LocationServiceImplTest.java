package com.web.gallery.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.model.common.LocationModelList;
import com.web.gallery.repository.impl.LocationMstRepositoryImpl;
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

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class LocationServiceImplTest {
  @InjectMocks private LocationServiceImpl locationServiceImpl;

  @Mock private LocationMstRepositoryImpl locationMstRepository;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getLocationList {
    @Test
    @Order(1)
    @DisplayName("正常系：Repositoryが返したロケーション一覧をそのまま返すこと")
    void getLocationList_success() {
      AccountNo accountNo = new AccountNo(1L);
      LocationModelList expected = LocationModelList.empty();
      doReturn(expected).when(locationMstRepository).getListByAccount(accountNo);

      LocationModelList result = locationServiceImpl.getLocationList(accountNo);

      assertEquals(expected, result);
      verify(locationMstRepository).getListByAccount(accountNo);
    }
  }
}
