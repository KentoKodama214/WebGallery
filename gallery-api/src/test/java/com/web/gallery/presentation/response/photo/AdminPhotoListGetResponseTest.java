package com.web.gallery.presentation.response.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.application.model.photo.AdminPhotoModel;
import com.web.gallery.application.model.photo.AdminPhotoModelList;
import com.web.gallery.application.model.photo.AdminPhotoPageModel;
import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.account.AccountName;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.ImageFilePath;
import com.web.gallery.domain.model.photo.IsHiddenByAdmin;
import com.web.gallery.domain.model.photo.PhotoJapaneseTitle;
import com.web.gallery.domain.model.photo.PhotoNo;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class AdminPhotoListGetResponseTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：写真一覧・isLastをそのままレスポンスに変換し、各項目を設定すること")
    void from_success() {
      OffsetDateTime createdAt = OffsetDateTime.parse("2026-11-01T10:00:00+09:00");
      AdminPhotoModel model =
          AdminPhotoModel.builder()
              .accountNo(new AccountNo(1L))
              .accountId(new AccountId("testuser01"))
              .accountName(new AccountName("テストユーザー"))
              .photoNo(new PhotoNo(3L))
              .photoJapaneseTitle(new PhotoJapaneseTitle("タイトル"))
              .imageFilePath(new ImageFilePath("https://signed.example/1/3-abc.jpg"))
              .isHiddenByAdmin(new IsHiddenByAdmin(true))
              .createdAt(createdAt)
              .build();

      AdminPhotoListGetResponse actual =
          AdminPhotoListGetResponse.from(
              AdminPhotoPageModel.of(AdminPhotoModelList.of(List.of(model)), false));

      assertFalse(actual.getIsLast());
      assertEquals(1, actual.getPhotoList().size());
      AdminPhotoListItemResponse item = actual.getPhotoList().getFirst();
      assertEquals(1L, item.getAccountNo());
      assertEquals("testuser01", item.getAccountId());
      assertEquals("テストユーザー", item.getAccountName());
      assertEquals(3L, item.getPhotoNo());
      assertEquals("タイトル", item.getPhotoJapaneseTitle());
      assertEquals("https://signed.example/1/3-abc.jpg", item.getImageFilePath());
      assertTrue(item.getIsHiddenByAdmin());
      assertEquals(createdAt, item.getCreatedAt());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：写真が0件の場合、空の一覧を返すこと")
    void from_empty() {
      AdminPhotoListGetResponse actual =
          AdminPhotoListGetResponse.from(AdminPhotoPageModel.of(AdminPhotoModelList.empty(), true));

      assertTrue(actual.getIsLast());
      assertTrue(actual.getPhotoList().isEmpty());
    }
  }
}
