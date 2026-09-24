package com.web.gallery.dto;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.model.photo.PhotoDetailSearchModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PhotoDetailGetDtoTest {

  private PhotoDetailSearchModel.PhotoDetailSearchModelBuilder baseBuilder() {
    return PhotoDetailSearchModel.builder()
        .photoAccountNo(new AccountNo(1L))
        .photoNo(new PhotoNo(1L));
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：ログイン中のアカウント番号が設定されている場合、その値が設定されること")
    void from_accountNoSet() {
      PhotoDetailSearchModel model = baseBuilder().accountNo(new AccountNo(2L)).build();

      PhotoDetailGetDto actual = PhotoDetailGetDto.from(model);

      assertEquals(2L, actual.getAccountNo());
      assertEquals(1L, actual.getPhotoAccountNo());
      assertEquals(1L, actual.getPhotoNo());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：ログイン中のアカウント番号が未設定の場合、nullが設定されること")
    void from_accountNoNull() {
      PhotoDetailSearchModel model = baseBuilder().accountNo(null).build();

      PhotoDetailGetDto actual = PhotoDetailGetDto.from(model);

      assertNull(actual.getAccountNo());
    }
  }
}
