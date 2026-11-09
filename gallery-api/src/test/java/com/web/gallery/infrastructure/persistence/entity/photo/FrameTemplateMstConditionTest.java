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
class FrameTemplateMstConditionTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class byAccountAndFrameTemplate {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号・額縁テンプレート番号が設定されること")
    void byAccountAndFrameTemplate_success() {
      FrameTemplateMstCondition actual =
          FrameTemplateMstCondition.byAccountAndFrameTemplate(1L, 2L);

      assertEquals(1L, actual.getAccountNo());
      assertEquals(2L, actual.getFrameTemplateNo());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class byAccountNo {
    @Test
    @Order(1)
    @DisplayName("正常系：アカウント番号のみが設定され、額縁テンプレート番号は条件外（null）となること")
    void byAccountNo_success() {
      FrameTemplateMstCondition actual = FrameTemplateMstCondition.byAccountNo(1L);

      assertEquals(1L, actual.getAccountNo());
      assertNull(actual.getFrameTemplateNo());
    }
  }
}
