package com.web.gallery.application.model.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.PhotoNo;
import com.web.gallery.domain.model.photo.TagEnglishName;
import com.web.gallery.domain.model.photo.TagJapaneseName;
import com.web.gallery.domain.model.photo.TagNo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PhotoTagModelTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class forRegist {
    @Test
    @Order(1)
    @DisplayName("セキュリティ：元のPhotoTagModelのアカウント番号ではなく、指定した写真所有者のアカウント番号が採用されること")
    void forRegist_overridesAccountNoWithPhotoOwner() {
      PhotoTagModel source =
          PhotoTagModel.builder()
              .accountNo(new AccountNo(999L))
              .tagJapaneseName(new TagJapaneseName("風景"))
              .tagEnglishName(new TagEnglishName("landscape"))
              .build();

      PhotoTagModel actual =
          PhotoTagModel.forRegist(source, new AccountNo(1L), new PhotoNo(2L), new TagNo(3L));

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new PhotoNo(2L), actual.getPhotoNo());
      assertEquals(new TagNo(3L), actual.getTagNo());
      assertEquals("風景", actual.getTagJapaneseName().value());
      assertEquals("landscape", actual.getTagEnglishName().value());
    }
  }
}
