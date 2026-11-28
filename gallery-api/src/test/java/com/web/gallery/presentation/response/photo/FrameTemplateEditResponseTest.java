package com.web.gallery.presentation.response.photo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class FrameTemplateEditResponseTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class of {
    @Test
    @Order(1)
    @DisplayName("正常系：HTTPステータス200・成功で、指定したメッセージ・額縁テンプレート番号が設定されること")
    void of_success() {
      FrameTemplateEditResponse actual = FrameTemplateEditResponse.of("登録しました。", 3L);

      assertEquals(200, actual.getHttpStatus());
      assertTrue(actual.getIsSuccess());
      assertEquals("登録しました。", actual.getMessage());
      assertEquals(3L, actual.getFrameTemplateNo());
    }
  }
}
