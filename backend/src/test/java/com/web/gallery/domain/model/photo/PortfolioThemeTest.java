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
class PortfolioThemeTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class constructor {
    @Test
    @Order(1)
    @DisplayName("正常系：空白でない値を指定した場合、インスタンスが生成されること")
    void constructor_success() {
      assertEquals("日本の自然", new PortfolioTheme("日本の自然").value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：100文字（上限）を指定した場合、インスタンスが生成されること")
    void constructor_maxLength() {
      String value = "あ".repeat(100);

      assertEquals(value, new PortfolioTheme(value).value());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_null() {
      assertThrows(IllegalArgumentException.class, () -> new PortfolioTheme(null));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：空白文字のみを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_blank() {
      assertThrows(IllegalArgumentException.class, () -> new PortfolioTheme("   "));
    }

    @Test
    @Order(5)
    @DisplayName("異常系：101文字（上限超過）を指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_tooLong() {
      String value = "あ".repeat(101);

      assertThrows(IllegalArgumentException.class, () -> new PortfolioTheme(value));
    }
  }
}
