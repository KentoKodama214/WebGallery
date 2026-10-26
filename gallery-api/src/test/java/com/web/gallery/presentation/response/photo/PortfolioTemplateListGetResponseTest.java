package com.web.gallery.presentation.response.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PortfolioTemplateListGetResponseTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class of {
    @Test
    @Order(1)
    @DisplayName("正常系：定義済みのテンプレートと、グリッド型の行パターン（枚数を含む）が定義順に設定されること")
    void of_success() {
      PortfolioTemplateListGetResponse actual = PortfolioTemplateListGetResponse.of();

      assertEquals(
          List.of(PortfolioTemplateEnum.FEATURE, PortfolioTemplateEnum.GRID),
          actual.getTemplateList().stream()
              .map(PortfolioTemplateItemResponse::getTemplateKbn)
              .toList());
      assertEquals(
          List.of("フィーチャー", "グリッド"),
          actual.getTemplateList().stream()
              .map(PortfolioTemplateItemResponse::getDisplayName)
              .toList());
      assertEquals(
          List.of("TWO_EQUAL", "THREE_EQUAL", "WIDE_NARROW", "NARROW_WIDE"),
          actual.getGridRowPatternList().stream()
              .map(GridRowPatternItemResponse::getPattern)
              .toList());
      assertEquals(
          List.of(2, 3, 2, 2),
          actual.getGridRowPatternList().stream()
              .map(GridRowPatternItemResponse::getPhotoCount)
              .toList());
      assertEquals(
          List.of("2枚均等", "3枚均等", "横長1枚+縦長1枚（縦長が右）", "縦長1枚+横長1枚（縦長が左）"),
          actual.getGridRowPatternList().stream()
              .map(GridRowPatternItemResponse::getDisplayName)
              .toList());
    }
  }
}
