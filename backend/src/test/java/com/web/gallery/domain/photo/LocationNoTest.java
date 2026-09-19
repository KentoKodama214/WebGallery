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
class LocationNoTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class constructor {
    @Test
    @Order(1)
    @DisplayName("正常系：0以上の値を指定した場合、インスタンスが生成されること")
    void constructor_success() {
      LocationNo actual = new LocationNo(0L);

      assertEquals(0L, actual.value());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_null() {
      assertThrows(IllegalArgumentException.class, () -> new LocationNo(null));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：負の値を指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_negative() {
      assertThrows(IllegalArgumentException.class, () -> new LocationNo(-1L));
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
      LocationNo locationNo = new LocationNo(1L);

      LocationNo actual = LocationNo.getOrDefault(locationNo);

      assertSame(locationNo, actual);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：nullの場合、デフォルト値を返すこと")
    void getOrDefault_null() {
      LocationNo actual = LocationNo.getOrDefault(null);

      assertEquals(new LocationNo(0L), actual);
    }
  }
}
