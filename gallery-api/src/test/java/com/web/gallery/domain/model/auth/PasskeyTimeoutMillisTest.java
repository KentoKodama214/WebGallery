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
class PasskeyTimeoutMillisTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class constructor {
    @Test
    @Order(1)
    @DisplayName("正常系：有効な値を指定した場合、インスタンスが生成されること")
    void constructor_success() {
      PasskeyTimeoutMillis actual = new PasskeyTimeoutMillis(300000L);

      assertEquals(300000L, actual.value());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_null() {
      assertThrows(IllegalArgumentException.class, () -> new PasskeyTimeoutMillis(null));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：0を指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_zero() {
      assertThrows(IllegalArgumentException.class, () -> new PasskeyTimeoutMillis(0L));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：負の値を指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_negative() {
      assertThrows(IllegalArgumentException.class, () -> new PasskeyTimeoutMillis(-1L));
    }
  }
}
