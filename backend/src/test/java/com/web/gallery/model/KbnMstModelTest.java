package com.web.gallery.model;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.entity.KbnMst;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class KbnMstModelTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、そのまま値が反映されること")
    void from_allFieldsSet() {
      KbnMst entity =
          KbnMst.builder()
              .kbnClassCode("sex")
              .kbnCode("man")
              .createdBy(1L)
              .createdAt(OffsetDateTime.now())
              .sortOrder(1)
              .kbnGroupCode("group")
              .kbnClassJapaneseName("性別")
              .kbnGroupJapaneseName("グループ")
              .kbnJapaneseName("男性")
              .kbnClassEnglishName("sex")
              .kbnGroupEnglishName("group")
              .kbnEnglishName("man")
              .explanation("説明")
              .build();

      KbnMstModel actual = KbnMstModel.from(entity);

      assertEquals("sex", actual.getKbnClassCode().value());
      assertEquals("man", actual.getKbnCode().value());
      assertEquals(1, actual.getSortOrder().value());
      assertEquals("group", actual.getKbnGroupCode().value());
      assertEquals("性別", actual.getKbnClassJapaneseName().value());
      assertEquals("グループ", actual.getKbnGroupJapaneseName().value());
      assertEquals("男性", actual.getKbnJapaneseName().value());
      assertEquals("sex", actual.getKbnClassEnglishName().value());
      assertEquals("group", actual.getKbnGroupEnglishName().value());
      assertEquals("man", actual.getKbnEnglishName().value());
      assertEquals("説明", actual.getExplanation().value());
    }
  }
}
