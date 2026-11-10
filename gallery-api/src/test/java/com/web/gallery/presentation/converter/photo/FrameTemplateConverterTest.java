package com.web.gallery.presentation.converter.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.application.model.photo.FrameTemplateModel;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.FrameDefinition;
import com.web.gallery.domain.model.photo.FrameTemplateName;
import com.web.gallery.domain.model.photo.FrameTemplateNo;
import com.web.gallery.presentation.request.photo.FrameTemplateSaveRequest;
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
class FrameTemplateConverterTest {

  private final FrameTemplateConverter converter = new FrameTemplateConverter();

  private static final Map<String, Object> DEFINITION =
      Map.of(
          "size", Map.of("width", 364, "height", 257),
          "slots", List.of(Map.of("x", 10, "y", 10, "width", 30, "height", 40)));

  private static FrameTemplateSaveRequest request(String templateName) {
    FrameTemplateSaveRequest request = new FrameTemplateSaveRequest();
    request.setTemplateName(templateName);
    request.setFrameDefinition(DEFINITION);
    return request;
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class toFrameTemplateModelForRegist {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目を設定した場合、セッションのアカウント番号とリクエストの値が転写され、額縁テンプレート番号は未設定となること")
    void toFrameTemplateModel_allFieldsSet() {
      FrameTemplateModel actual =
          converter.toFrameTemplateModel(request("四つ切 3枚組"), new AccountNo(1L));

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new FrameTemplateName("四つ切 3枚組"), actual.getTemplateName());
      assertEquals(new FrameDefinition(DEFINITION), actual.getFrameDefinition());
      assertNull(actual.getFrameTemplateNo());
      assertNull(actual.getCreatedAt());
      assertNull(actual.getUpdatedAt());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：テンプレート名の前後の空白を除くこと")
    void toFrameTemplateModel_stripName() {
      FrameTemplateModel actual =
          converter.toFrameTemplateModel(request("　四つ切 "), new AccountNo(1L));

      assertEquals(new FrameTemplateName("四つ切"), actual.getTemplateName());
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class toFrameTemplateModelForUpdate {
    @Test
    @Order(1)
    @DisplayName("正常系：パスパラメータの額縁テンプレート番号が設定されること")
    void toFrameTemplateModel_withFrameTemplateNo() {
      FrameTemplateModel actual =
          converter.toFrameTemplateModel(
              request("四つ切 3枚組"), new AccountNo(1L), new FrameTemplateNo(2L));

      assertEquals(new FrameTemplateNo(2L), actual.getFrameTemplateNo());
      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new FrameTemplateName("四つ切 3枚組"), actual.getTemplateName());
      assertEquals(new FrameDefinition(DEFINITION), actual.getFrameDefinition());
    }
  }
}
