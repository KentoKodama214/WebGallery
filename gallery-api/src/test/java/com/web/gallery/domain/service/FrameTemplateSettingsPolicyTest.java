package com.web.gallery.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.web.gallery.domain.model.photo.FrameDefinition;
import com.web.gallery.domain.model.photo.PhotoCount;
import com.web.gallery.domain.model.photo.TemplateSettings;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class FrameTemplateSettingsPolicyTest {

  @InjectMocks private FrameTemplateSettingsPolicy policy;

  @Mock private FrameDefinitionPolicy frameDefinitionPolicy;

  private static final Map<String, Object> FRAME =
      Map.of("size", Map.of("width", 364, "height", 257));

  /** 額縁定義に、指定した項目を上書き・追加したポートフォリオ単位の設定 */
  private static TemplateSettings settings(Map<String, Object> overrides) {
    Map<String, Object> value = new HashMap<>();
    value.put("frame", FRAME);
    value.putAll(overrides);
    return new TemplateSettings(value);
  }

  @BeforeEach
  void setUp() {
    // 額縁定義そのものの妥当性・枠の数はFrameDefinitionPolicyに委譲する（メッセージ枠2件・写真枠3件の妥当な額縁）
    lenient().doReturn(true).when(frameDefinitionPolicy).isValid(new FrameDefinition(FRAME));
    lenient().doReturn(2).when(frameDefinitionPolicy).countTexts(new FrameDefinition(FRAME));
    lenient().doReturn(3).when(frameDefinitionPolicy).countSlots(new FrameDefinition(FRAME));
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class isValidTemplateSettings {
    @Test
    @Order(1)
    @DisplayName("正常系：額縁定義のみの場合、妥当と判定すること")
    void isValidTemplateSettings_frameOnly() {
      assertTrue(policy.isValidTemplateSettings(settings(Map.of())));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：コピー元の額縁テンプレート番号・メッセージ枠の数と同数の文面を設定した場合、妥当と判定すること")
    void isValidTemplateSettings_allFields() {
      assertTrue(
          policy.isValidTemplateSettings(
              settings(Map.of("frameTemplateNo", 3, "messages", List.of("文面1", "")))));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：未知の項目を含む場合、不正と判定すること")
    void isValidTemplateSettings_unknownKey() {
      assertFalse(policy.isValidTemplateSettings(settings(Map.of("rows", List.of()))));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：額縁定義が未設定・オブジェクトでない場合、額縁定義を判定せずに不正と判定すること")
    void isValidTemplateSettings_missingFrame() {
      assertFalse(policy.isValidTemplateSettings(TemplateSettings.empty()));
      assertFalse(policy.isValidTemplateSettings(settings(Map.of("frame", List.of()))));
      verify(frameDefinitionPolicy, never()).isValid(any());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：額縁定義が不正な場合、不正と判定すること")
    void isValidTemplateSettings_invalidFrame() {
      doReturn(false).when(frameDefinitionPolicy).isValid(new FrameDefinition(FRAME));

      assertFalse(policy.isValidTemplateSettings(settings(Map.of())));
    }

    @Test
    @Order(6)
    @DisplayName("異常系：コピー元の額縁テンプレート番号が正の整数でない場合、不正と判定すること")
    void isValidTemplateSettings_invalidFrameTemplateNo() {
      assertFalse(policy.isValidTemplateSettings(settings(Map.of("frameTemplateNo", 0))));
      assertFalse(policy.isValidTemplateSettings(settings(Map.of("frameTemplateNo", 1.5))));
      assertFalse(policy.isValidTemplateSettings(settings(Map.of("frameTemplateNo", "1"))));
    }

    @Test
    @Order(7)
    @DisplayName("異常系：文面がメッセージ枠の数を超える場合、不正と判定すること")
    void isValidTemplateSettings_tooManyMessages() {
      assertFalse(
          policy.isValidTemplateSettings(settings(Map.of("messages", List.of("1", "2", "3")))));
    }

    @Test
    @Order(8)
    @DisplayName("正常系：文面が上限（200文字）ちょうどの場合、妥当と判定すること")
    void isValidTemplateSettings_messageAtLimit() {
      assertTrue(
          policy.isValidTemplateSettings(settings(Map.of("messages", List.of("あ".repeat(200))))));
    }

    @Test
    @Order(9)
    @DisplayName("異常系：文面が上限超過（201文字）・文字列でない・配列でない場合、不正と判定すること")
    void isValidTemplateSettings_invalidMessage() {
      assertFalse(
          policy.isValidTemplateSettings(settings(Map.of("messages", List.of("あ".repeat(201))))));
      assertFalse(policy.isValidTemplateSettings(settings(Map.of("messages", List.of(1)))));
      assertFalse(policy.isValidTemplateSettings(settings(Map.of("messages", "文面"))));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class isValidPhotoSettings {
    @Test
    @Order(1)
    @DisplayName("正常系：写真単位の設定が空の場合、妥当と判定すること")
    void isValidPhotoSettings_empty() {
      assertTrue(policy.isValidPhotoSettings(TemplateSettings.empty()));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：写真単位の設定に項目がある場合、不正と判定すること")
    void isValidPhotoSettings_notEmpty() {
      assertFalse(policy.isValidPhotoSettings(new TemplateSettings(Map.of("theme", "テーマ"))));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class isPublishable {
    @Test
    @Order(1)
    @DisplayName("正常系：写真枠の数と写真の枚数が一致する場合、公開できると判定すること")
    void isPublishable_slotCountMatches() {
      assertTrue(policy.isPublishable(settings(Map.of()), new PhotoCount(3)));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：写真の枚数が写真枠の数より少ない・多い場合、公開できないと判定すること")
    void isPublishable_slotCountMismatch() {
      assertFalse(policy.isPublishable(settings(Map.of()), new PhotoCount(2)));
      assertFalse(policy.isPublishable(settings(Map.of()), new PhotoCount(4)));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：写真が0枚の場合、公開できないと判定すること")
    void isPublishable_noPhoto() {
      assertFalse(policy.isPublishable(settings(Map.of()), new PhotoCount(0)));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：設定が不正な場合、公開できないと判定すること")
    void isPublishable_invalidSettings() {
      assertFalse(policy.isPublishable(TemplateSettings.empty(), new PhotoCount(3)));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class isPublishablePhotoSettings {
    @Test
    @Order(1)
    @DisplayName("正常系：写真単位の設定によらず、公開できると判定すること")
    void isPublishablePhotoSettings_always() {
      assertTrue(policy.isPublishablePhotoSettings(TemplateSettings.empty()));
    }
  }
}
