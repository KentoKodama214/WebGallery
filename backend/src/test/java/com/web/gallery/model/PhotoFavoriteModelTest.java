package com.web.gallery.model;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.controller.request.PhotoFavoriteDeleteRequest;
import com.web.gallery.controller.request.PhotoFavoriteRegistRequest;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PhotoFavoriteModelTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class fromByPhotoFavoriteRegistRequest {
    @Test
    @Order(1)
    @DisplayName("正常系：登録リクエストの各項目が値オブジェクトに変換されて設定されること")
    void from_success() {
      PhotoFavoriteRegistRequest request = new PhotoFavoriteRegistRequest();
      request.setFavoritePhotoAccountNo(2L);
      request.setFavoritePhotoNo(3L);

      PhotoFavoriteModel actual = PhotoFavoriteModel.from(request, 1L);

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new AccountNo(2L), actual.getFavoritePhotoAccountNo());
      assertEquals(new PhotoNo(3L), actual.getFavoritePhotoNo());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class fromByPhotoFavoriteDeleteRequest {
    @Test
    @Order(1)
    @DisplayName("正常系：解除リクエストの各項目が値オブジェクトに変換されて設定されること")
    void from_success() {
      PhotoFavoriteDeleteRequest request = new PhotoFavoriteDeleteRequest();
      request.setFavoritePhotoAccountNo(2L);
      request.setFavoritePhotoNo(3L);

      PhotoFavoriteModel actual = PhotoFavoriteModel.from(request, 1L);

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new AccountNo(2L), actual.getFavoritePhotoAccountNo());
      assertEquals(new PhotoNo(3L), actual.getFavoritePhotoNo());
    }
  }
}
