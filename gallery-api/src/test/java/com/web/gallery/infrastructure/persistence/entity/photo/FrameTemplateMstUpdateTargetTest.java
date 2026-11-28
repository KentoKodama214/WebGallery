package com.web.gallery.infrastructure.persistence.entity.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.application.model.photo.FrameTemplateModel;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.FrameDefinition;
import com.web.gallery.domain.model.photo.FrameTemplateName;
import com.web.gallery.domain.model.photo.FrameTemplateNo;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class FrameTemplateMstUpdateTargetTest {

  private static final Map<String, Object> DEFINITION =
      Map.of(
          "size", Map.of("width", 364, "height", 257),
          "slots", List.of(Map.of("x", 10, "y", 10, "width", 30, "height", 40)));

  private static FrameTemplateModel model() {
    return FrameTemplateModel.builder()
        .accountNo(new AccountNo(1L))
        .frameTemplateNo(new FrameTemplateNo(2L))
        .templateName(new FrameTemplateName("四つ切 3枚組"))
        .frameDefinition(new FrameDefinition(DEFINITION))
        .build();
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class forUpdate {
    @Test
    @Order(1)
    @DisplayName("正常系：更新者・テンプレート名・額縁定義が設定されること")
    void forUpdate_success() {
      FrameTemplateMstUpdateTarget actual = FrameTemplateMstUpdateTarget.forUpdate(model());

      assertEquals(1L, actual.getUpdatedBy());
      assertEquals("四つ切 3枚組", actual.getTemplateName());
      assertEquals(DEFINITION, actual.getFrameDefinition());
    }
  }
}
