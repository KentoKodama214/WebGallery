package com.web.gallery.entity;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.model.PhotoFavoriteModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PhotoFavoriteTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、そのまま値が反映されること")
    void from_allFieldsPresent() {
      PhotoFavoriteModel model =
          PhotoFavoriteModel.builder()
              .accountNo(new AccountNo(1L))
              .favoritePhotoAccountNo(new AccountNo(2L))
              .favoritePhotoNo(new PhotoNo(3L))
              .build();

      PhotoFavorite actual = PhotoFavorite.from(model);

      assertEquals(1L, actual.getAccountNo());
      assertEquals(2L, actual.getFavoritePhotoAccountNo());
      assertEquals(3L, actual.getFavoritePhotoNo());
      assertEquals(1L, actual.getCreatedBy());
    }
  }
}
