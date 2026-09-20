package com.web.gallery.model.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountId;
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
class PhotoDetailGetModelTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：ログイン中のアカウント番号が設定されている場合、AccountNoに変換されること")
    void from_accountNoSet() {
      IpAddress ipAddress = new IpAddress("203.0.113.1");
      Referer referer = new Referer("https://example.com/");

      PhotoDetailGetModel actual = PhotoDetailGetModel.from(1L, "aaaaaaaa", 2L, ipAddress, referer);

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new AccountId("aaaaaaaa"), actual.getPhotoAccountId());
      assertEquals(new PhotoNo(2L), actual.getPhotoNo());
      assertEquals(ipAddress, actual.getIpAddress());
      assertEquals(referer, actual.getReferer());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：ログイン中のアカウント番号が未設定の場合、nullが設定されること")
    void from_accountNoNull() {
      PhotoDetailGetModel actual =
          PhotoDetailGetModel.from(
              null, "aaaaaaaa", 2L, new IpAddress("203.0.113.1"), new Referer(""));

      assertNull(actual.getAccountNo());
    }
  }
}
