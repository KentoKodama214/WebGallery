package com.web.gallery.domain.photo;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class ShutterSpeedTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class constructor {
    @Test
    @Order(1)
    @DisplayName("正常系：0以上の値を指定した場合、インスタンスが生成されること")
    void constructor_success() {
      ShutterSpeed actual = new ShutterSpeed(BigDecimal.ZERO);

      assertEquals(BigDecimal.ZERO, actual.value());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_null() {
      assertThrows(IllegalArgumentException.class, () -> new ShutterSpeed(null));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：負の値を指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_negative() {
      assertThrows(
          IllegalArgumentException.class, () -> new ShutterSpeed(BigDecimal.valueOf(-0.1)));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getOrDefault {
    @Test
    @Order(1)
    @DisplayName("正常系：nullでない場合、そのままの値を返すこと")
    void getOrDefault_notNull() {
      ShutterSpeed shutterSpeed = new ShutterSpeed(BigDecimal.valueOf(0.01));

      ShutterSpeed actual = ShutterSpeed.getOrDefault(shutterSpeed);

      assertSame(shutterSpeed, actual);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：nullの場合、デフォルト値を返すこと")
    void getOrDefault_null() {
      ShutterSpeed actual = ShutterSpeed.getOrDefault(null);

      assertEquals(new ShutterSpeed(BigDecimal.ZERO), actual);
    }
  }
}
