package com.web.gallery.model.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.controller.request.photo.PhotoTagSaveRequest;
import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.photo.TagEnglishName;
import com.web.gallery.domain.photo.TagJapaneseName;
import com.web.gallery.domain.photo.TagNo;
import com.web.gallery.entity.photo.PhotoTagMst;
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
  class fromPhotoTagMst {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が値オブジェクトへ変換されてそのまま反映されること")
    void from_allFieldsSet() {
      PhotoTagMst entity =
          PhotoTagMst.builder()
              .accountNo(1L)
              .photoNo(2L)
              .tagNo(3L)
              .tagJapaneseName("風景")
              .tagEnglishName("landscape")
              .build();

      PhotoTagModel actual = PhotoTagModel.from(entity);

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new PhotoNo(2L), actual.getPhotoNo());
      assertEquals(new TagNo(3L), actual.getTagNo());
      assertEquals("風景", actual.getTagJapaneseName().value());
      assertEquals("landscape", actual.getTagEnglishName().value());
    }
  }

  @Nested
  @Order(2)
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

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class fromPhotoTagSaveRequest {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、そのまま値が反映されること")
    void from_allFieldsSet() {
      PhotoTagSaveRequest request = new PhotoTagSaveRequest();
      request.setTagJapaneseName("風景");
      request.setTagEnglishName("landscape");

      PhotoTagModel actual = PhotoTagModel.from(request, new AccountNo(1L));

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals("風景", actual.getTagJapaneseName().value());
      assertEquals("landscape", actual.getTagEnglishName().value());
      assertNull(actual.getPhotoNo());
      assertNull(actual.getTagNo());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：タグ英語名が未設定の場合、空文字が設定されること")
    void from_tagEnglishNameNull() {
      PhotoTagSaveRequest request = new PhotoTagSaveRequest();
      request.setTagJapaneseName("風景");
      request.setTagEnglishName(null);

      PhotoTagModel actual = PhotoTagModel.from(request, new AccountNo(1L));

      assertEquals("", actual.getTagEnglishName().value());
    }
  }
}
