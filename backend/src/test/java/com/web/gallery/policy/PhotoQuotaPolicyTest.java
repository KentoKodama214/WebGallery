package com.web.gallery.policy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.web.gallery.config.PhotoConfig;
import com.web.gallery.domain.photo.PhotoCount;
import com.web.gallery.enumeration.AuthorityEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PhotoQuotaPolicyTest {
  @InjectMocks private PhotoQuotaPolicy photoQuotaPolicy;

  @Mock private PhotoConfig photoConfig;

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
