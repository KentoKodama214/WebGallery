package com.web.gallery.application.model.photo;

import static org.junit.jupiter.api.Assertions.*;

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
class AdminPhotoGetModelTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class of {
    @Test
    @Order(1)
    @DisplayName("正常系：1ページ目の場合、offsetが0でlimitが表示件数+1になり、絞り込み条件が引き継がれること")
    void of_firstPage() {
      AdminPhotoListGetModel adminPhotoListGetModel =
          AdminPhotoListGetModel.builder()
              .accountId(new AccountId("aaaaaaaa"))
              .isHiddenByAdmin(new IsHiddenByAdmin(true))
              .pageNo(1)
              .build();

      AdminPhotoGetModel actual = AdminPhotoGetModel.of(adminPhotoListGetModel, 20);

      assertEquals(new AccountId("aaaaaaaa"), actual.getAccountId());
      assertEquals(new IsHiddenByAdmin(true), actual.getIsHiddenByAdmin());
      assertEquals(21, actual.getLimit());
      assertEquals(0, actual.getOffset());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：2ページ目以降の場合、pageNoに応じてoffsetが計算され、未設定の絞り込み条件はnullのままであること")
    void of_secondPage() {
      AdminPhotoListGetModel adminPhotoListGetModel =
          AdminPhotoListGetModel.builder().pageNo(3).build();

      AdminPhotoGetModel actual = AdminPhotoGetModel.of(adminPhotoListGetModel, 20);

      assertNull(actual.getAccountId());
      assertNull(actual.getIsHiddenByAdmin());
      assertEquals(21, actual.getLimit());
      assertEquals(40, actual.getOffset());
    }
  }
}
