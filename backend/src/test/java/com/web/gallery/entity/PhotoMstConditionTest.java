package com.web.gallery.entity;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.ImageFile;
import com.web.gallery.domain.photo.ImageFilePath;
import com.web.gallery.model.PhotoDetailModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PhotoMstConditionTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class forExistCheck {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号と画像ファイルのオリジナルファイル名が設定されること")
    void forExistCheck_success() {
      PhotoDetailModel model =
          PhotoDetailModel.builder()
              .accountNo(new AccountNo(1L))
              .imageFilePath(new ImageFilePath("dummy/path.jpg"))
              .imageFile(
                  new ImageFile(
                      new MockMultipartFile(
                          "file", "original.jpg", "image/jpeg", new byte[] {1, 2, 3})))
              .build();

      PhotoMstCondition actual = PhotoMstCondition.forExistCheck(model);

      assertEquals(1L, actual.getAccountNo());
      assertEquals("original.jpg", actual.getImageFileName());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：オリジナルファイル名にディレクトリ区切りを含む場合、末尾のファイル名のみが設定されること")
    void forExistCheck_originalFilenameWithPath() {
      PhotoDetailModel model =
          PhotoDetailModel.builder()
              .accountNo(new AccountNo(1L))
              .imageFilePath(new ImageFilePath("dummy/path.jpg"))
              .imageFile(
                  new ImageFile(
                      new MockMultipartFile(
                          "file", "sub/dir/original.jpg", "image/jpeg", new byte[] {1, 2, 3})))
              .build();

      PhotoMstCondition actual = PhotoMstCondition.forExistCheck(model);

      assertEquals("original.jpg", actual.getImageFileName());
    }
  }
}
