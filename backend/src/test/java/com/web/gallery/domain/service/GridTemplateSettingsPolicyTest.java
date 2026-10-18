package com.web.gallery.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.model.photo.PhotoCount;
import com.web.gallery.domain.model.photo.TemplateSettings;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class GridTemplateSettingsPolicyTest {
  private final GridTemplateSettingsPolicy policy = new GridTemplateSettingsPolicy();

  /**
   * 指定した行パターンを並べたテンプレート設定を生成する
   *
   * @param patterns 行パターン名
   * @return {@link TemplateSettings}
   */
  private static TemplateSettings rows(String... patterns) {
    return new TemplateSettings(
        Map.of(
            "rows", Arrays.stream(patterns).map(pattern -> Map.of("pattern", pattern)).toList()));
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class isValidTemplateSettings {
    @Test
    @Order(1)
    @DisplayName("正常系：既知の行パターンだけを並べた場合、trueを返すこと")
    void isValidTemplateSettings_knownPatterns() {
      assertTrue(
          policy.isValidTemplateSettings(
              rows("TWO_EQUAL", "THREE_EQUAL", "WIDE_NARROW", "NARROW_WIDE")));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：項目が1つもない場合（下書きで未設定）、trueを返すこと")
    void isValidTemplateSettings_empty() {
      assertTrue(policy.isValidTemplateSettings(TemplateSettings.empty()));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：行数が上限（100行）ちょうどの場合、trueを返すこと")
    void isValidTemplateSettings_exactlyAtLimit() {
      assertTrue(
          policy.isValidTemplateSettings(
              rows(Collections.nCopies(100, "TWO_EQUAL").toArray(String[]::new))));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：行数が上限（100行）を超える場合、falseを返すこと")
    void isValidTemplateSettings_tooManyRows() {
      assertFalse(
          policy.isValidTemplateSettings(
              rows(Collections.nCopies(101, "TWO_EQUAL").toArray(String[]::new))));
    }

    @Test
    @Order(5)
    @DisplayName("異常系：未知の行パターンを含む場合、falseを返すこと")
    void isValidTemplateSettings_unknownPattern() {
      assertFalse(policy.isValidTemplateSettings(rows("TWO_EQUAL", "FOUR_EQUAL")));
    }

    @Test
    @Order(6)
    @DisplayName("異常系：行に行パターン以外の項目を含む場合、falseを返すこと")
    void isValidTemplateSettings_unknownRowKey() {
      assertFalse(
          policy.isValidTemplateSettings(
              new TemplateSettings(
                  Map.of("rows", List.of(Map.of("pattern", "TWO_EQUAL", "size", 1))))));
    }

    @Test
    @Order(7)
    @DisplayName("異常系：行の一覧がリストでない場合、falseを返すこと")
    void isValidTemplateSettings_rowsNotList() {
      assertFalse(
          policy.isValidTemplateSettings(new TemplateSettings(Map.of("rows", "TWO_EQUAL"))));
    }

    @Test
    @Order(8)
    @DisplayName("異常系：行がオブジェクトでない場合、falseを返すこと")
    void isValidTemplateSettings_rowNotMap() {
      assertFalse(
          policy.isValidTemplateSettings(
              new TemplateSettings(Map.of("rows", List.of("TWO_EQUAL")))));
    }

    @Test
    @Order(9)
    @DisplayName("異常系：未知の項目を含む場合、falseを返すこと")
    void isValidTemplateSettings_unknownKey() {
      assertFalse(policy.isValidTemplateSettings(new TemplateSettings(Map.of("columns", 2))));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class isValidPhotoSettings {
    @Test
    @Order(1)
    @DisplayName("正常系：キャプションを上限文字数ちょうどで指定した場合、trueを返すこと")
    void isValidPhotoSettings_exactlyAtLimit() {
      assertTrue(
          policy.isValidPhotoSettings(new TemplateSettings(Map.of("caption", "あ".repeat(100)))));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：項目が1つもない場合、trueを返すこと")
    void isValidPhotoSettings_empty() {
      assertTrue(policy.isValidPhotoSettings(TemplateSettings.empty()));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：キャプションが上限文字数を超える場合、falseを返すこと")
    void isValidPhotoSettings_tooLong() {
      assertFalse(
          policy.isValidPhotoSettings(new TemplateSettings(Map.of("caption", "あ".repeat(101)))));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：キャプションが文字列でない場合、falseを返すこと")
    void isValidPhotoSettings_notString() {
      assertFalse(policy.isValidPhotoSettings(new TemplateSettings(Map.of("caption", true))));
    }

    @Test
    @Order(5)
    @DisplayName("異常系：未知の項目（他テンプレートの項目を含む）が指定された場合、falseを返すこと")
    void isValidPhotoSettings_unknownKey() {
      assertFalse(policy.isValidPhotoSettings(new TemplateSettings(Map.of("theme", "x"))));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class isPublishable {
    @Test
    @Order(1)
    @DisplayName("正常系：行パターンの枚数の合計（2+3+2=7）と写真の枚数が一致する場合、trueを返すこと")
    void isPublishable_exactlyMatched() {
      assertTrue(
          policy.isPublishable(rows("TWO_EQUAL", "THREE_EQUAL", "WIDE_NARROW"), new PhotoCount(7)));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：行パターンの枚数の合計より写真が1枚少ない場合、falseを返すこと")
    void isPublishable_photoShort() {
      assertFalse(
          policy.isPublishable(rows("TWO_EQUAL", "THREE_EQUAL", "WIDE_NARROW"), new PhotoCount(6)));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：行パターンの枚数の合計より写真が1枚多い場合、falseを返すこと")
    void isPublishable_photoExcess() {
      assertFalse(
          policy.isPublishable(rows("TWO_EQUAL", "THREE_EQUAL", "WIDE_NARROW"), new PhotoCount(8)));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：写真が1枚も設定されていない場合、falseを返すこと")
    void isPublishable_noPhoto() {
      assertFalse(policy.isPublishable(rows(), new PhotoCount(0)));
    }

    @Test
    @Order(5)
    @DisplayName("異常系：行の一覧が未設定の場合、falseを返すこと")
    void isPublishable_noRows() {
      assertFalse(policy.isPublishable(TemplateSettings.empty(), new PhotoCount(2)));
    }

    @Test
    @Order(6)
    @DisplayName("異常系：設定の構造が不正な場合、falseを返すこと")
    void isPublishable_invalidSettings() {
      assertFalse(policy.isPublishable(rows("TWO_EQUAL", "UNKNOWN"), new PhotoCount(2)));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class isPublishablePhotoSettings {
    @Test
    @Order(1)
    @DisplayName("正常系：キャプションは任意項目のため、未設定でもtrueを返すこと")
    void isPublishablePhotoSettings_empty() {
      assertTrue(policy.isPublishablePhotoSettings(TemplateSettings.empty()));
    }
  }
}
