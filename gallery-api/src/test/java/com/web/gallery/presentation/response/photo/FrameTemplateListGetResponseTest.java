package com.web.gallery.presentation.response.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.application.model.photo.FrameTemplateModel;
import com.web.gallery.application.model.photo.FrameTemplateModelList;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.FrameDefinition;
import com.web.gallery.domain.model.photo.FrameTemplateName;
import com.web.gallery.domain.model.photo.FrameTemplateNo;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
class FrameTemplateListGetResponseTest {

  private static final OffsetDateTime CREATED_AT =
      OffsetDateTime.of(2000, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(9));

  private static final OffsetDateTime UPDATED_AT =
      OffsetDateTime.of(2001, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(9));

  private static final Map<String, Object> DEFINITION =
      Map.of("size", Map.of("width", 364, "height", 257));

  private static FrameTemplateModel model(Long frameTemplateNo) {
    return FrameTemplateModel.builder()
        .accountNo(new AccountNo(1L))
        .frameTemplateNo(new FrameTemplateNo(frameTemplateNo))
        .templateName(new FrameTemplateName("額縁" + frameTemplateNo))
        .frameDefinition(new FrameDefinition(DEFINITION))
        .createdAt(CREATED_AT)
        .updatedAt(UPDATED_AT)
        .build();
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：各額縁テンプレートの全項目が、一覧の順のまま転写されること")
    void from_success() {
      FrameTemplateListGetResponse actual =
          FrameTemplateListGetResponse.from(
              FrameTemplateModelList.of(List.of(model(1L), model(2L))));

      assertEquals(2, actual.getFrameTemplateList().size());
      FrameTemplateItemResponse first = actual.getFrameTemplateList().get(0);
      assertEquals(1L, first.getFrameTemplateNo());
      assertEquals("額縁1", first.getTemplateName());
      assertEquals(DEFINITION, first.getFrameDefinition());
      assertEquals(CREATED_AT, first.getCreatedAt());
      assertEquals(UPDATED_AT, first.getUpdatedAt());
      assertEquals(2L, actual.getFrameTemplateList().get(1).getFrameTemplateNo());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：一覧が空の場合、空のリストになること")
    void from_empty() {
      assertEquals(
          List.of(),
          FrameTemplateListGetResponse.from(FrameTemplateModelList.empty()).getFrameTemplateList());
    }

    @Test
    @Order(3)
    @DisplayName("異常系：作成日時・更新日時が未設定の場合、nullになること")
    void from_optionalFieldsNull() {
      FrameTemplateModel model = model(1L).toBuilder().createdAt(null).updatedAt(null).build();

      FrameTemplateItemResponse actual =
          FrameTemplateListGetResponse.from(FrameTemplateModelList.of(List.of(model)))
              .getFrameTemplateList()
              .get(0);

      assertNull(actual.getCreatedAt());
      assertNull(actual.getUpdatedAt());
    }
  }
}
