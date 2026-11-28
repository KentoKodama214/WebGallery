package com.web.gallery.infrastructure.persistence.entity.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.application.aggregate.Photo;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.ModerationReason;
import com.web.gallery.domain.model.photo.PhotoNo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PhotoModerationLogTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：非公開にした場合、写真・操作した管理者・理由が設定され、非公開フラグがtrueになること")
    void from_hide() {
      Photo photo = Photo.forModeration(new AccountNo(1L), new PhotoNo(5L));
      photo.hideByAdmin(new AccountNo(99L), new ModerationReason("不適切な画像のため"));

      PhotoModerationLog actual = PhotoModerationLog.from(photo);

      assertNull(actual.getPhotoModerationLogNo());
      assertEquals(1L, actual.getPhotoAccountNo());
      assertEquals(5L, actual.getPhotoNo());
      assertEquals(99L, actual.getAdminAccountNo());
      assertTrue(actual.getIsHidden());
      assertEquals("不適切な画像のため", actual.getReason());
      assertEquals(99L, actual.getCreatedBy());
      // 作成日時は未設定（DB側でNOW()を設定する）
      assertNull(actual.getCreatedAt());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：非公開を解除した場合、非公開フラグがfalseになること")
    void from_unhide() {
      Photo photo = Photo.forModeration(new AccountNo(1L), new PhotoNo(5L));
      photo.unhideByAdmin(new AccountNo(99L), new ModerationReason("誤判定だったため"));

      PhotoModerationLog actual = PhotoModerationLog.from(photo);

      assertFalse(actual.getIsHidden());
      assertEquals("誤判定だったため", actual.getReason());
    }
  }
}
