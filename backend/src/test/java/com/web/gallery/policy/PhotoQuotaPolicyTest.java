package com.web.gallery.policy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.web.gallery.config.PhotoConfig;
import com.web.gallery.domain.photo.PhotoCount;
import com.web.gallery.enumeration.AuthorityEnum;
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
public class PhotoQuotaPolicyTest {
  @InjectMocks private PhotoQuotaPolicy photoQuotaPolicy;

  @Mock private PhotoConfig photoConfig;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class isReachedByCurrentCount {
    @Test
    @Order(1)
    @DisplayName("正常系：mini-userで、上限まで登録済みの場合")
    void isReached_mini_user_reached() {
      doReturn(10).when(photoConfig).getMiniUserUpperLimit();
      assertTrue(photoQuotaPolicy.isReached(AuthorityEnum.MINI, new PhotoCount(10)));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：mini-userで、上限まで未登録の場合")
    void isReached_mini_user_not_reached() {
      doReturn(10).when(photoConfig).getMiniUserUpperLimit();
      assertFalse(photoQuotaPolicy.isReached(AuthorityEnum.MINI, new PhotoCount(9)));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：normal-userで、上限まで登録済みの場合")
    void isReached_normal_user_reached() {
      doReturn(1000).when(photoConfig).getNormalUserUpperLimit();
      assertTrue(photoQuotaPolicy.isReached(AuthorityEnum.NORMAL, new PhotoCount(1000)));
    }

    @Test
    @Order(4)
    @DisplayName("正常系：normal-userで、上限まで未登録の場合")
    void isReached_normal_user_not_reached() {
      doReturn(1000).when(photoConfig).getNormalUserUpperLimit();
      assertFalse(photoQuotaPolicy.isReached(AuthorityEnum.NORMAL, new PhotoCount(999)));
    }

    @Test
    @Order(5)
    @DisplayName("正常系：special-userの場合")
    void isReached_special_user() {
      assertFalse(photoQuotaPolicy.isReached(AuthorityEnum.SPECIAL, new PhotoCount(1000)));
    }

    @Test
    @Order(6)
    @DisplayName("正常系：administratorの場合")
    void isReached_administrator() {
      assertFalse(photoQuotaPolicy.isReached(AuthorityEnum.ADMINISTRATOR, new PhotoCount(1000)));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class isReachedByCurrentCountAndRequestedCount {
    @Test
    @Order(1)
    @DisplayName("正常系：一括登録判定で、登録後の枚数が上限を超える場合")
    void isReached_bulk_mini_user_exceeds() {
      doReturn(10).when(photoConfig).getMiniUserUpperLimit();
      assertTrue(
          photoQuotaPolicy.isReached(AuthorityEnum.MINI, new PhotoCount(8), new PhotoCount(3)));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：一括登録判定で、登録後の枚数がちょうど上限に達する場合")
    void isReached_bulk_mini_user_reaches_exactly() {
      doReturn(10).when(photoConfig).getMiniUserUpperLimit();
      assertFalse(
          photoQuotaPolicy.isReached(AuthorityEnum.MINI, new PhotoCount(8), new PhotoCount(2)));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：一括登録判定で、special-userの場合は上限を超えない")
    void isReached_bulk_special_user() {
      assertFalse(
          photoQuotaPolicy.isReached(
              AuthorityEnum.SPECIAL, new PhotoCount(1000), new PhotoCount(100)));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class remainingCount {
    @Test
    @Order(1)
    @DisplayName("正常系：mini-userの残り登録可能枚数を取得する場合")
    void remainingCount_mini_user() {
      doReturn(10).when(photoConfig).getMiniUserUpperLimit();
      assertEquals(3, photoQuotaPolicy.remainingCount(AuthorityEnum.MINI, new PhotoCount(7)));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：mini-userで登録済み枚数が上限を超えている場合、残り登録可能枚数は0")
    void remainingCount_mini_user_over_limit() {
      doReturn(10).when(photoConfig).getMiniUserUpperLimit();
      assertEquals(0, photoQuotaPolicy.remainingCount(AuthorityEnum.MINI, new PhotoCount(12)));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：normal-userの残り登録可能枚数を取得する場合")
    void remainingCount_normal_user() {
      doReturn(1000).when(photoConfig).getNormalUserUpperLimit();
      assertEquals(1, photoQuotaPolicy.remainingCount(AuthorityEnum.NORMAL, new PhotoCount(999)));
    }

    @Test
    @Order(4)
    @DisplayName("正常系：special-userの残り登録可能枚数は無制限（null）")
    void remainingCount_special_user() {
      assertNull(photoQuotaPolicy.remainingCount(AuthorityEnum.SPECIAL, new PhotoCount(1000)));
    }

    @Test
    @Order(5)
    @DisplayName("正常系：administratorの残り登録可能枚数は無制限（null）")
    void remainingCount_administrator() {
      assertNull(
          photoQuotaPolicy.remainingCount(AuthorityEnum.ADMINISTRATOR, new PhotoCount(1000)));
    }
  }
}
