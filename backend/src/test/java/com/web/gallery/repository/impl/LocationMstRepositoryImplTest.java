package com.web.gallery.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.entity.common.LocationMst;
import com.web.gallery.entity.common.LocationMstCondition;
import com.web.gallery.mapper.LocationMstMapper;
import com.web.gallery.model.common.LocationModelList;
import java.math.BigDecimal;
import java.util.List;
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
public class LocationMstRepositoryImplTest {
  @InjectMocks private LocationMstRepositoryImpl locationMstRepositoryImpl;

  @Mock private LocationMstMapper locationMstMapper;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getListByAccount {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号で抽出条件を組み立て、取得したロケーション一覧を返すこと")
    void getListByAccount_success() {
      AccountNo accountNo = new AccountNo(1L);
      LocationMst locationMst =
          LocationMst.builder()
              .accountNo(1L)
              .locationNo(3L)
              .managementName("渋谷スクランブル交差点_管理用")
              .displayName("渋谷スクランブル交差点")
              .address("東京都渋谷区")
              .latitude(new BigDecimal("35.6812"))
              .longitude(new BigDecimal("139.7671"))
              .build();

      ArgumentCaptor<LocationMstCondition> conditionCaptor =
          ArgumentCaptor.forClass(LocationMstCondition.class);
      doReturn(List.of(locationMst)).when(locationMstMapper).select(conditionCaptor.capture());

      LocationModelList result = locationMstRepositoryImpl.getListByAccount(accountNo);

      assertEquals(1L, conditionCaptor.getValue().getAccountNo());
      assertEquals(1, result.size());
      assertEquals(3L, result.get(0).getLocationNo().value());
      assertEquals("渋谷スクランブル交差点_管理用", result.get(0).getManagementName().value());
      assertEquals("渋谷スクランブル交差点", result.get(0).getDisplayName().value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：該当するロケーションが存在しない場合、空のLocationModelListを返すこと")
    void getListByAccount_empty() {
      AccountNo accountNo = new AccountNo(1L);
      doReturn(List.of()).when(locationMstMapper).select(any(LocationMstCondition.class));

      LocationModelList result = locationMstRepositoryImpl.getListByAccount(accountNo);

      assertTrue(result.isEmpty());
    }
  }
}
