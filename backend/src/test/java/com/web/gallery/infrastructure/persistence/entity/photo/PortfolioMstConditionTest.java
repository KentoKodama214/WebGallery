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
class PortfolioMstConditionTest {
  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class byAccountAndPortfolioNotDeleted {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・ポートフォリオ番号と、削除済みを除外する条件が設定されること")
    void byAccountAndPortfolioNotDeleted_success() {
      PortfolioMstCondition actual = PortfolioMstCondition.byAccountAndPortfolioNotDeleted(1L, 2L);

      assertEquals(1L, actual.getAccountNo());
      assertEquals(2L, actual.getPortfolioNo());
      assertFalse(actual.getIsDeleted());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class byAccountNoNotDeleted {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号と、削除済みを除外する条件のみが設定されること")
    void byAccountNoNotDeleted_success() {
      PortfolioMstCondition actual = PortfolioMstCondition.byAccountNoNotDeleted(1L);

      assertEquals(1L, actual.getAccountNo());
      assertNull(actual.getPortfolioNo());
      assertFalse(actual.getIsDeleted());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class byAccountNo {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号のみが設定され、削除済みも対象に含むこと")
    void byAccountNo_success() {
      PortfolioMstCondition actual = PortfolioMstCondition.byAccountNo(1L);

      assertEquals(1L, actual.getAccountNo());
      assertNull(actual.getPortfolioNo());
      assertNull(actual.getIsDeleted());
    }
  }
}
