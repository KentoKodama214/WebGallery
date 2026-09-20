package com.web.gallery.controller.response.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.photo.TagEnglishName;
import com.web.gallery.domain.photo.TagJapaneseName;
import com.web.gallery.domain.photo.TagNo;
import com.web.gallery.model.photo.PhotoTagModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PhotoTagResponseTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：photoNo・tagNoが設定されている場合、それぞれの値が設定されること")
    void from_withPhotoNoAndTagNo() {
      PhotoTagModel model =
          PhotoTagModel.builder()
              .accountNo(new AccountNo(1L))
              .photoNo(new PhotoNo(2L))
              .tagNo(new TagNo(3L))
              .tagJapaneseName(new TagJapaneseName("風景"))
              .tagEnglishName(new TagEnglishName("landscape"))
              .build();

      PhotoTagResponse actual = PhotoTagResponse.from(model);

      assertEquals(1L, actual.getAccountNo());
      assertEquals(2L, actual.getPhotoNo());
      assertEquals(3L, actual.getTagNo());
      assertEquals("風景", actual.getTagJapaneseName());
      assertEquals("landscape", actual.getTagEnglishName());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：photoNo・tagNoが未設定の場合、それぞれnullが設定されること")
    void from_withoutPhotoNoAndTagNo() {
      PhotoTagModel model =
          PhotoTagModel.builder()
              .accountNo(new AccountNo(1L))
              .tagJapaneseName(new TagJapaneseName("風景"))
              .tagEnglishName(new TagEnglishName("landscape"))
              .build();

      PhotoTagResponse actual = PhotoTagResponse.from(model);

      assertNull(actual.getPhotoNo());
      assertNull(actual.getTagNo());
    }
  }
}
