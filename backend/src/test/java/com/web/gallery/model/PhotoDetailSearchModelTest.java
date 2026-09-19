package com.web.gallery.model;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.common.IpAddress;
import com.web.gallery.domain.common.Referer;
import com.web.gallery.domain.photo.PhotoNo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PhotoDetailSearchModelTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class of {
    @Test
    @Order(1)
    @DisplayName("正常系：ログイン中のアカウント番号が設定されている場合、その値が転写されること")
    void of_accountNoSet() {
      PhotoDetailGetModel getModel =
          PhotoDetailGetModel.from(
              1L, "aaaaaaaa", 2L, new IpAddress("203.0.113.1"), new Referer(""));

      PhotoDetailSearchModel actual = PhotoDetailSearchModel.of(getModel, new AccountNo(9L));

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new AccountNo(9L), actual.getPhotoAccountNo());
      assertEquals(new PhotoNo(2L), actual.getPhotoNo());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：ログイン中のアカウント番号が未設定の場合、nullが転写されること")
    void of_accountNoNull() {
      PhotoDetailGetModel getModel =
          PhotoDetailGetModel.from(
              null, "aaaaaaaa", 2L, new IpAddress("203.0.113.1"), new Referer(""));

      PhotoDetailSearchModel actual = PhotoDetailSearchModel.of(getModel, new AccountNo(9L));

      assertNull(actual.getAccountNo());
    }
  }
}
