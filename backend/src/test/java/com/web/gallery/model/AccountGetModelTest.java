package com.web.gallery.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class AccountGetModelTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class of {
    @Test
    @Order(1)
    @DisplayName("正常系：1ページ目の場合、offsetは0、limitは表示件数+1になること")
    void of_firstPage() {
      AccountListGetModel accountListGetModel = AccountListGetModel.builder().pageNo(1).build();

      AccountGetModel actual = AccountGetModel.of(accountListGetModel, 20);

      assertEquals(21, actual.getLimit());
      assertEquals(0, actual.getOffset());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：2ページ目以降の場合、offsetがページ番号に応じて計算されること")
    void of_secondPage() {
      AccountListGetModel accountListGetModel = AccountListGetModel.builder().pageNo(3).build();

      AccountGetModel actual = AccountGetModel.of(accountListGetModel, 20);

      assertEquals(21, actual.getLimit());
      assertEquals(40, actual.getOffset());
    }
  }
}
