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
class PortfolioDescriptionTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class constructor {
    @Test
    @Order(1)
    @DisplayName("正常系：値を指定した場合、インスタンスが生成されること")
    void constructor_success() {
      assertEquals("説明文", new PortfolioDescription("説明文").value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：空文字を指定した場合、インスタンスが生成されること")
    void constructor_empty() {
      assertEquals("", new PortfolioDescription("").value());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：2000文字（上限）を指定した場合、インスタンスが生成されること")
    void constructor_maxLength() {
      String value = "あ".repeat(2000);

      assertEquals(value, new PortfolioDescription(value).value());
    }

    @Test
    @Order(4)
    @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_null() {
      assertThrows(IllegalArgumentException.class, () -> new PortfolioDescription(null));
    }

    @Test
    @Order(5)
    @DisplayName("異常系：2001文字（上限超過）を指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_tooLong() {
      String value = "あ".repeat(2001);

      assertThrows(IllegalArgumentException.class, () -> new PortfolioDescription(value));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getOrDefault {
    @Test
    @Order(1)
    @DisplayName("正常系：nullでない場合、同一インスタンスを返すこと")
    void getOrDefault_nonNull() {
      PortfolioDescription description = new PortfolioDescription("説明文");

      assertSame(description, PortfolioDescription.getOrDefault(description));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：nullの場合、空文字を持つインスタンスを返すこと")
    void getOrDefault_null() {
      assertEquals(new PortfolioDescription(""), PortfolioDescription.getOrDefault(null));
    }
  }
}
