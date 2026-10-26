package com.web.gallery.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.model.photo.PhotoCount;
import com.web.gallery.domain.model.photo.TemplateSettings;
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
class FeatureTemplateSettingsPolicyTest {
  private final FeatureTemplateSettingsPolicy policy = new FeatureTemplateSettingsPolicy();

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class isValidTemplateSettings {
    @Test
    @Order(1)
    @DisplayName("正常系：項目が1つもない場合、trueを返すこと")
    void isValidTemplateSettings_empty() {
      assertTrue(policy.isValidTemplateSettings(TemplateSettings.empty()));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：ポートフォリオ単位の設定を持たないテンプレートに項目が指定された場合、falseを返すこと")
    void isValidTemplateSettings_hasKeys() {
      assertFalse(policy.isValidTemplateSettings(new TemplateSettings(Map.of("rows", "x"))));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class isValidPhotoSettings {
    @Test
    @Order(1)
    @DisplayName("正常系：テーマ・サブテーマ・説明を上限文字数ちょうどで指定した場合、trueを返すこと")
    void isValidPhotoSettings_exactlyAtLimit() {
      assertTrue(
          policy.isValidPhotoSettings(
              new TemplateSettings(
                  Map.of(
                      "theme", "あ".repeat(100),
                      "subTheme", "あ".repeat(100),
                      "description", "あ".repeat(2000)))));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：項目が1つもない場合（下書きで未入力）、trueを返すこと")
    void isValidPhotoSettings_empty() {
      assertTrue(policy.isValidPhotoSettings(TemplateSettings.empty()));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：テーマが上限文字数を超える場合、falseを返すこと")
    void isValidPhotoSettings_themeTooLong() {
      assertFalse(
          policy.isValidPhotoSettings(new TemplateSettings(Map.of("theme", "あ".repeat(101)))));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：サブテーマが上限文字数を超える場合、falseを返すこと")
    void isValidPhotoSettings_subThemeTooLong() {
      assertFalse(
          policy.isValidPhotoSettings(new TemplateSettings(Map.of("subTheme", "あ".repeat(101)))));
    }

    @Test
    @Order(5)
    @DisplayName("異常系：説明が上限文字数を超える場合、falseを返すこと")
    void isValidPhotoSettings_descriptionTooLong() {
      assertFalse(
          policy.isValidPhotoSettings(
              new TemplateSettings(Map.of("description", "あ".repeat(2001)))));
    }

    @Test
    @Order(6)
    @DisplayName("異常系：文字列以外の値が指定された場合、falseを返すこと")
    void isValidPhotoSettings_notString() {
      assertFalse(policy.isValidPhotoSettings(new TemplateSettings(Map.of("theme", 1))));
    }

    @Test
    @Order(7)
    @DisplayName("異常系：未知の項目（他テンプレートの項目を含む）が指定された場合、falseを返すこと")
    void isValidPhotoSettings_unknownKey() {
      assertFalse(policy.isValidPhotoSettings(new TemplateSettings(Map.of("caption", "x"))));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class isPublishable {
    @Test
    @Order(1)
    @DisplayName("正常系：写真が1枚設定されている場合、trueを返すこと")
    void isPublishable_onePhoto() {
      assertTrue(policy.isPublishable(TemplateSettings.empty(), new PhotoCount(1)));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：写真が1枚も設定されていない場合、falseを返すこと")
    void isPublishable_noPhoto() {
      assertFalse(policy.isPublishable(TemplateSettings.empty(), new PhotoCount(0)));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class isPublishablePhotoSettings {
    @Test
    @Order(1)
    @DisplayName("正常系：テーマが入力されている場合、trueを返すこと")
    void isPublishablePhotoSettings_hasTheme() {
      assertTrue(policy.isPublishablePhotoSettings(new TemplateSettings(Map.of("theme", "日本の自然"))));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：テーマが未設定の場合、falseを返すこと")
    void isPublishablePhotoSettings_noTheme() {
      assertFalse(
          policy.isPublishablePhotoSettings(new TemplateSettings(Map.of("subTheme", "サブ"))));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：テーマが空白のみの場合、falseを返すこと")
    void isPublishablePhotoSettings_blankTheme() {
      assertFalse(policy.isPublishablePhotoSettings(new TemplateSettings(Map.of("theme", "  "))));
    }
  }
}
