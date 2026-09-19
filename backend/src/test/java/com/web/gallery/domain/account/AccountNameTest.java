package com.web.gallery.domain.account;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class AccountNameTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class constructor {
    @Test
    @Order(1)
    @DisplayName("正常系：空白でない値を指定した場合、インスタンスが生成されること")
    void constructor_success() {
      AccountName actual = new AccountName("山田太郎");

      assertEquals("山田太郎", actual.value());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_null() {
      assertThrows(IllegalArgumentException.class, () -> new AccountName(null));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：空白文字のみを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_blank() {
      assertThrows(IllegalArgumentException.class, () -> new AccountName("   "));
    }
  }
}
