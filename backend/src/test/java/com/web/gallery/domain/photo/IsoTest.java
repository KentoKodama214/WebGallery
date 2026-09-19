package com.web.gallery.domain.photo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class IsoTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class constructor {
    @Test
    @Order(1)
    @DisplayName("正常系：0以上の値を指定した場合、インスタンスが生成されること")
    void constructor_success() {
      Iso actual = new Iso(0);

      assertEquals(0, actual.value());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_null() {
      assertThrows(IllegalArgumentException.class, () -> new Iso(null));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：負の値を指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_negative() {
      assertThrows(IllegalArgumentException.class, () -> new Iso(-1));
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
      Iso iso = new Iso(100);

      Iso actual = Iso.getOrDefault(iso);

      assertSame(iso, actual);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：nullの場合、デフォルト値を返すこと")
    void getOrDefault_null() {
      Iso actual = Iso.getOrDefault(null);

      assertEquals(new Iso(0), actual);
    }
  }
}
