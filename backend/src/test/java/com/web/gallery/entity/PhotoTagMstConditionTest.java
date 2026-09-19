package com.web.gallery.entity;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.enumeration.DirectionEnum;
import com.web.gallery.enumeration.SortPhotoEnum;
import com.web.gallery.model.PhotoDetailSearchModel;
import com.web.gallery.model.PhotoGetModel;
import com.web.gallery.model.PhotoTagDeleteModel;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PhotoTagMstConditionTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class fromByPhotoTagDeleteModel {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・写真番号がそのまま反映されること")
    void from_success() {
      PhotoTagDeleteModel model = PhotoTagDeleteModel.of(new AccountNo(1L), new PhotoNo(2L));

      PhotoTagMstCondition actual = PhotoTagMstCondition.from(model);

      assertEquals(1L, actual.getAccountNo());
      assertEquals(2L, actual.getPhotoNo());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class fromByPhotoDetailSearchModel {
    @Test
    @Order(1)
    @DisplayName("正常系：写真のアカウント番号・写真番号がそのまま反映されること")
    void from_success() {
      PhotoDetailSearchModel model =
          PhotoDetailSearchModel.builder()
              .photoAccountNo(new AccountNo(1L))
              .photoNo(new PhotoNo(2L))
              .build();

      PhotoTagMstCondition actual = PhotoTagMstCondition.from(model);

      assertEquals(1L, actual.getAccountNo());
      assertEquals(2L, actual.getPhotoNo());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class fromByPhotoGetModelAndPhotoNoList {
    private PhotoGetModel.PhotoGetModelBuilder baseBuilder() {
      return PhotoGetModel.builder()
          .photoAccountNo(new AccountNo(1L))
          .directionKbn(DirectionEnum.NONE)
          .tagList(List.of())
          .sortBy(SortPhotoEnum.PHOTO_AT)
          .limit(21)
          .offset(0);
    }

    @Test
    @Order(1)
    @DisplayName("正常系：写真のアカウント番号と指定した写真番号リストが反映されること")
    void from_withPhotoNoList() {
      PhotoGetModel model = baseBuilder().build();

      PhotoTagMstCondition actual = PhotoTagMstCondition.from(model, List.of(1L, 2L, 3L));

      assertEquals(1L, actual.getAccountNo());
      assertEquals(List.of(1L, 2L, 3L), actual.getPhotoNoList());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：写真番号リストが空の場合、空リストが反映されること")
    void from_withEmptyPhotoNoList() {
      PhotoGetModel model = baseBuilder().build();

      PhotoTagMstCondition actual = PhotoTagMstCondition.from(model, List.of());

      assertTrue(actual.getPhotoNoList().isEmpty());
    }
  }
}
