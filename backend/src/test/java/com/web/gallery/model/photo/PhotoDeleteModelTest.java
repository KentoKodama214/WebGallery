package com.web.gallery.model.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.controller.request.photo.PhotoDeleteRequest;
import com.web.gallery.domain.account.AccountNo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PhotoDeleteModelTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：リクエストとアカウント番号からModelが生成されること")
    void from_success() {
      PhotoDeleteRequest request = new PhotoDeleteRequest();
      request.setPhotoNo(1L);
      request.setImageFilePath("1/1-abc.jpg");

      PhotoDeleteModel actual = PhotoDeleteModel.from(request, new AccountNo(2L));

      assertEquals(2L, actual.getAccountNo().value());
      assertEquals(1L, actual.getPhotoNo().value());
      assertEquals("1/1-abc.jpg", actual.getImageFilePath().value());
    }
  }
}
