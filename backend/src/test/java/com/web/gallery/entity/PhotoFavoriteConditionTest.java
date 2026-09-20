package com.web.gallery.entity;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.model.PhotoFavoriteDeleteModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PhotoFavoriteConditionTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、そのまま値が反映されること")
    void from_allFieldsPresent() {
      PhotoFavoriteDeleteModel model =
          PhotoFavoriteDeleteModel.builder()
              .accountNo(new AccountNo(1L))
              .favoritePhotoAccountNo(new AccountNo(2L))
              .favoritePhotoNo(new PhotoNo(3L))
              .build();

      PhotoFavoriteCondition actual = PhotoFavoriteCondition.from(model);

      assertEquals(1L, actual.getAccountNo());
      assertEquals(2L, actual.getFavoritePhotoAccountNo());
      assertEquals(3L, actual.getFavoritePhotoNo());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class forClear {
    @Test
    @Order(1)
    @DisplayName("正常系：お気に入り写真アカウント番号・写真番号のみ反映され、アカウント番号はnullのままであること")
    void forClear_accountNoNotSet() {
      PhotoFavoriteDeleteModel model =
          PhotoFavoriteDeleteModel.builder()
              .accountNo(new AccountNo(1L))
              .favoritePhotoAccountNo(new AccountNo(2L))
              .favoritePhotoNo(new PhotoNo(3L))
              .build();

      PhotoFavoriteCondition actual = PhotoFavoriteCondition.forClear(model);

      assertNull(actual.getAccountNo());
      assertEquals(2L, actual.getFavoritePhotoAccountNo());
      assertEquals(3L, actual.getFavoritePhotoNo());
    }
  }
}
