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
class LoginFailureCountTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class constructor {
    @Test
    @Order(1)
    @DisplayName("正常系：0を指定した場合、インスタンスが生成されること")
    void constructor_zero() {
      LoginFailureCount actual = new LoginFailureCount(0);

      assertEquals(0, actual.value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：正の値を指定した場合、インスタンスが生成されること")
    void constructor_positive() {
      LoginFailureCount actual = new LoginFailureCount(3);

      assertEquals(3, actual.value());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_null() {
      assertThrows(IllegalArgumentException.class, () -> new LoginFailureCount(null));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：負の値を指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_negative() {
      assertThrows(IllegalArgumentException.class, () -> new LoginFailureCount(-1));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getOrDefault {
    @Test
    @Order(1)
    @DisplayName("正常系：nullでない場合、そのまま返すこと")
    void getOrDefault_notNull() {
      LoginFailureCount value = new LoginFailureCount(2);

      LoginFailureCount actual = LoginFailureCount.getOrDefault(value);

      assertSame(value, actual);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：nullの場合、デフォルト値を持つインスタンスを返すこと")
    void getOrDefault_null() {
      LoginFailureCount actual = LoginFailureCount.getOrDefault(null);

      assertEquals(0, actual.value());
    }
  }
}
