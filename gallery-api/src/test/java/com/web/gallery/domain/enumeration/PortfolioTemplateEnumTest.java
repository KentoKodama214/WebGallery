package com.web.gallery.domain.enumeration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PortfolioTemplateEnumTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class fromValue {
    @Test
    @Order(1)
    @DisplayName("正常系：DB保存値に一致する場合、対応するEnum値を返すこと")
    void fromValue_matchedByDbValue() {
      assertEquals(PortfolioTemplateEnum.FEATURE, PortfolioTemplateEnum.fromValue("feature"));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：Enum名に一致する場合、対応するEnum値を返すこと")
    void fromValue_matchedByName() {
      assertEquals(PortfolioTemplateEnum.GRID, PortfolioTemplateEnum.fromValue("GRID"));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：nullを指定した場合、別の値として扱わずnullを返すこと")
    void fromValue_null() {
      assertNull(PortfolioTemplateEnum.fromValue(null));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：該当するEnum値がない場合、別の値として扱わずnullを返すこと")
    void fromValue_unmatched() {
      assertNull(PortfolioTemplateEnum.fromValue("unknown"));
    }
  }
}
