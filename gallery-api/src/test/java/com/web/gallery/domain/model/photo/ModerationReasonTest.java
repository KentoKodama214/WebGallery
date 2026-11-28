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
class ModerationReasonTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class constructor {
    @Test
    @Order(1)
    @DisplayName("正常系：空白でない値を指定した場合、インスタンスが生成されること")
    void constructor_success() {
      ModerationReason actual = new ModerationReason("不適切な画像との報告があったため");

      assertEquals("不適切な画像との報告があったため", actual.value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：500文字（上限）を指定した場合、インスタンスが生成されること")
    void constructor_maxLength() {
      String value = "a".repeat(500);

      ModerationReason actual = new ModerationReason(value);

      assertEquals(value, actual.value());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_null() {
      assertThrows(IllegalArgumentException.class, () -> new ModerationReason(null));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：空白文字のみを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_blank() {
      assertThrows(IllegalArgumentException.class, () -> new ModerationReason("   "));
    }

    @Test
    @Order(5)
    @DisplayName("異常系：501文字（上限超過）を指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_tooLong() {
      String value = "a".repeat(501);

      assertThrows(IllegalArgumentException.class, () -> new ModerationReason(value));
    }
  }
}
