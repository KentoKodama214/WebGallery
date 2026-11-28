package com.web.gallery.domain.model.photo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class FrameTemplateNoTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class constructor {
    @Test
    @Order(1)
    @DisplayName("正常系：正の値を指定した場合、インスタンスが生成されること")
    void constructor_success() {
      assertEquals(1L, new FrameTemplateNo(1L).value());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_null() {
      assertThrows(IllegalArgumentException.class, () -> new FrameTemplateNo(null));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：0を指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_zero() {
      assertThrows(IllegalArgumentException.class, () -> new FrameTemplateNo(0L));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：負の値を指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_negative() {
      assertThrows(IllegalArgumentException.class, () -> new FrameTemplateNo(-1L));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class next {
    @Test
    @Order(1)
    @DisplayName("正常系：登録済みの最大値がある場合、最大値+1を採番すること")
    void next_existing() {
      assertEquals(new FrameTemplateNo(4L), FrameTemplateNo.next(3L));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：未登録（null）の場合、1を採番すること")
    void next_null() {
      assertEquals(new FrameTemplateNo(1L), FrameTemplateNo.next(null));
    }
  }
}
