package com.web.gallery.entity.account;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.model.account.AccountGetModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class AccountConditionTest {

  private AccountGetModel accountGetModel() {
    return AccountGetModel.builder().limit(21).offset(20).build();
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class forList {
    @Test
    @Order(1)
    @DisplayName("正常系：limit・offsetがそのまま反映され、削除済みアカウントを除外する条件になること")
    void forList_success() {
      AccountCondition actual = AccountCondition.forList(accountGetModel());

      assertEquals(21, actual.getLimit());
      assertEquals(20, actual.getOffset());
      assertFalse(actual.getIsDeleted());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class forAdminList {
    @Test
    @Order(1)
    @DisplayName("正常系：limit・offsetがそのまま反映され、削除済みアカウントも含む条件（isDeleted未設定）になること")
    void forAdminList_success() {
      AccountCondition actual = AccountCondition.forAdminList(accountGetModel());

      assertEquals(21, actual.getLimit());
      assertEquals(20, actual.getOffset());
      assertNull(actual.getIsDeleted());
    }
  }
}
