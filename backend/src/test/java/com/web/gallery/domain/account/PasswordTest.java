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
class PasswordTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class constructor {
    @Test
    @Order(1)
    @DisplayName("正常系：空白でない値を指定した場合、インスタンスが生成されること")
    void constructor_success() {
      Password actual = new Password("P@ssw0rd");

      assertEquals("P@ssw0rd", actual.value());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_null() {
      assertThrows(IllegalArgumentException.class, () -> new Password(null));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：空白文字のみを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_blank() {
      assertThrows(IllegalArgumentException.class, () -> new Password("   "));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class toStringMethod {
    @Test
    @Order(1)
    @DisplayName("正常系：値を伏字にして返すこと")
    void toString_masked() {
      Password actual = new Password("P@ssw0rd");

      assertEquals("****", actual.toString());
    }
  }
}
