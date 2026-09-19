package com.web.gallery.entity;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.enumeration.AuthorityEnum;
import com.web.gallery.model.AccountModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class AccountAuthorityUpdateTargetTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class fromForUpdate {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、そのまま値が反映されること")
    void fromForUpdate_allFieldsPresent() {
      AccountModel model = AccountModel.builder().authorityKbn(AuthorityEnum.ADMINISTRATOR).build();

      AccountAuthorityUpdateTarget actual = AccountAuthorityUpdateTarget.fromForUpdate(model);

      assertEquals(0L, actual.getUpdatedBy());
      assertEquals(AuthorityEnum.ADMINISTRATOR, actual.getAuthorityKbn());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：未設定項目のみの場合、権限区分にnullが反映されること")
    void fromForUpdate_allFieldsAbsent() {
      AccountModel model = AccountModel.builder().build();

      AccountAuthorityUpdateTarget actual = AccountAuthorityUpdateTarget.fromForUpdate(model);

      assertEquals(0L, actual.getUpdatedBy());
      assertNull(actual.getAuthorityKbn());
    }
  }
}
