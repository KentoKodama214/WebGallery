package com.web.gallery.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.enumeration.AuthorityEnum;
import com.web.gallery.domain.model.photo.PortfolioCount;
import com.web.gallery.domain.model.photo.PortfolioUpperLimits;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PortfolioQuotaPolicyTest {
  private final PortfolioQuotaPolicy portfolioQuotaPolicy = new PortfolioQuotaPolicy();

  private static final PortfolioUpperLimits UPPER_LIMITS = new PortfolioUpperLimits(3, 10);

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class isReached {
    @Test
    @Order(1)
    @DisplayName("正常系：mini-userで、上限（3個）まで作成済みの場合、trueを返すこと")
    void isReached_miniUserReached() {
      assertTrue(
          portfolioQuotaPolicy.isReached(AuthorityEnum.MINI, new PortfolioCount(3), UPPER_LIMITS));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：mini-userで、上限より1個少ない場合、falseを返すこと")
    void isReached_miniUserNotReached() {
      assertFalse(
          portfolioQuotaPolicy.isReached(AuthorityEnum.MINI, new PortfolioCount(2), UPPER_LIMITS));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：normal-userで、上限（10個）まで作成済みの場合、trueを返すこと")
    void isReached_normalUserReached() {
      assertTrue(
          portfolioQuotaPolicy.isReached(
              AuthorityEnum.NORMAL, new PortfolioCount(10), UPPER_LIMITS));
    }

    @Test
    @Order(4)
    @DisplayName("正常系：normal-userで、上限より1個少ない場合、falseを返すこと")
    void isReached_normalUserNotReached() {
      assertFalse(
          portfolioQuotaPolicy.isReached(
              AuthorityEnum.NORMAL, new PortfolioCount(9), UPPER_LIMITS));
    }

    @Test
    @Order(5)
    @DisplayName("正常系：special-user・administratorは上限を持たないため、件数によらずfalseを返すこと")
    void isReached_unlimited() {
      assertFalse(
          portfolioQuotaPolicy.isReached(
              AuthorityEnum.SPECIAL, new PortfolioCount(1000), UPPER_LIMITS));
      assertFalse(
          portfolioQuotaPolicy.isReached(
              AuthorityEnum.ADMINISTRATOR, new PortfolioCount(1000), UPPER_LIMITS));
    }
  }
}
