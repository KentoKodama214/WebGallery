package com.web.gallery.model;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.controller.request.PhotoDeleteRequest;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.domain.photo.PhotoNo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PhotoFavoriteDeleteModelTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class fromByPhotoFavoriteModel {
    @Test
    @Order(1)
    @DisplayName("正常系：PhotoFavoriteModelの各項目がそのまま転写されること")
    void from_success() {
      PhotoFavoriteModel model =
          PhotoFavoriteModel.builder()
              .accountNo(new AccountNo(1L))
              .favoritePhotoAccountNo(new AccountNo(2L))
              .favoritePhotoNo(new PhotoNo(3L))
              .build();

      PhotoFavoriteDeleteModel actual = PhotoFavoriteDeleteModel.from(model);

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new AccountNo(2L), actual.getFavoritePhotoAccountNo());
      assertEquals(new PhotoNo(3L), actual.getFavoritePhotoNo());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class fromByPhotoDeleteModel {
    @Test
    @Order(1)
    @DisplayName(
        "正常系：PhotoDeleteModelのaccountNo・photoNoがfavoritePhotoAccountNo・favoritePhotoNoに転写され、accountNoはnullになること")
    void from_success() {
      PhotoDeleteRequest request = new PhotoDeleteRequest();
      request.setImageFilePath("path/to/image.jpg");
      PhotoDeleteModel model =
          PhotoDeleteModel.builder()
              .accountNo(new AccountNo(1L))
              .photoNo(new PhotoNo(2L))
              .imageFilePath(new ImageFilePath("path/to/image.jpg"))
              .build();

      PhotoFavoriteDeleteModel actual = PhotoFavoriteDeleteModel.from(model);

      assertNull(actual.getAccountNo());
      assertEquals(new AccountNo(1L), actual.getFavoritePhotoAccountNo());
      assertEquals(new PhotoNo(2L), actual.getFavoritePhotoNo());
    }
  }
}
