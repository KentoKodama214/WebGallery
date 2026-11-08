package com.web.gallery.infrastructure.persistence.dto.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.application.model.photo.AdminPhotoGetModel;
import com.web.gallery.domain.model.account.AccountId;
import com.web.gallery.domain.model.photo.IsHiddenByAdmin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class AdminPhotoListGetDtoTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：絞り込み条件が設定されている場合、それぞれの値が設定されること")
    void from_withFilter() {
      AdminPhotoGetModel model =
          AdminPhotoGetModel.builder()
              .accountId(new AccountId("aaaaaaaa"))
              .isHiddenByAdmin(new IsHiddenByAdmin(true))
              .limit(21)
              .offset(20)
              .build();

      AdminPhotoListGetDto actual = AdminPhotoListGetDto.from(model);

      assertEquals("aaaaaaaa", actual.getAccountId());
      assertTrue(actual.getIsHiddenByAdmin());
      assertEquals(21, actual.getLimit());
      assertEquals(20, actual.getOffset());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：絞り込み条件が未設定の場合、それぞれnullが設定されること")
    void from_withoutFilter() {
      AdminPhotoGetModel model = AdminPhotoGetModel.builder().limit(21).offset(0).build();

      AdminPhotoListGetDto actual = AdminPhotoListGetDto.from(model);

      assertNull(actual.getAccountId());
      assertNull(actual.getIsHiddenByAdmin());
      assertEquals(21, actual.getLimit());
      assertEquals(0, actual.getOffset());
    }
  }
}
