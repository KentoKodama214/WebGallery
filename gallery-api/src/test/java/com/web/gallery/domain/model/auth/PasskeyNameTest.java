package com.web.gallery.domain.model.auth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PasskeyNameTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class constructor {
    @Test
    @Order(1)
    @DisplayName("正常系：有効な値を指定した場合、インスタンスが生成されること")
    void constructor_success() {
      PasskeyName actual = new PasskeyName("MacBookのTouch ID");

      assertEquals("MacBookのTouch ID", actual.value());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_null() {
      assertThrows(IllegalArgumentException.class, () -> new PasskeyName(null));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：空白文字のみを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_blank() {
      assertThrows(IllegalArgumentException.class, () -> new PasskeyName("   "));
    }

    @Test
    @Order(4)
    @DisplayName("正常系：上限の50文字ちょうどを指定した場合、インスタンスが生成されること")
    void constructor_maxLength() {
      String value = "あ".repeat(50);

      assertEquals(value, new PasskeyName(value).value());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：50文字を超える値を指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_tooLong() {
      assertThrows(IllegalArgumentException.class, () -> new PasskeyName("あ".repeat(51)));
    }
  }
}
