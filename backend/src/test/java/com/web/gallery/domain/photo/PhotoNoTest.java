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
class PhotoNoTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class constructor {
    @Test
    @Order(1)
    @DisplayName("正常系：正の値を指定した場合、インスタンスが生成されること")
    void constructor_success() {
      PhotoNo actual = new PhotoNo(1L);

      assertEquals(1L, actual.value());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_null() {
      assertThrows(IllegalArgumentException.class, () -> new PhotoNo(null));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：0を指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_zero() {
      assertThrows(IllegalArgumentException.class, () -> new PhotoNo(0L));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：負の値を指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_negative() {
      assertThrows(IllegalArgumentException.class, () -> new PhotoNo(-1L));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class next {
    @Test
    @Order(1)
    @DisplayName("正常系：現在の最大写真番号がnullの場合、1を返すこと")
    void next_maxIsNull() {
      PhotoNo actual = PhotoNo.next(null);

      assertEquals(new PhotoNo(1L), actual);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：現在の最大写真番号が存在する場合、その値に1を加えた番号を返すこと")
    void next_maxExists() {
      PhotoNo actual = PhotoNo.next(5L);

      assertEquals(new PhotoNo(6L), actual);
    }
  }
}
