package com.web.gallery.infrastructure.persistence.entity.photo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PortfolioTagMstConditionTest {
  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class byAccountAndPortfolio {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・ポートフォリオ番号が設定されること")
    void byAccountAndPortfolio_success() {
      PortfolioTagMstCondition actual = PortfolioTagMstCondition.byAccountAndPortfolio(1L, 2L);

      assertEquals(1L, actual.getAccountNo());
      assertEquals(2L, actual.getPortfolioNo());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class byAccountNo {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号のみが設定されること")
    void byAccountNo_success() {
      PortfolioTagMstCondition actual = PortfolioTagMstCondition.byAccountNo(1L);

      assertEquals(1L, actual.getAccountNo());
      assertNull(actual.getPortfolioNo());
    }
  }
}
