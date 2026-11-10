package com.web.gallery.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.model.photo.FrameDefinition;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
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
class FrameDefinitionPolicyTest {

  private final FrameDefinitionPolicy policy = new FrameDefinitionPolicy();

  /** 必須項目のみの写真枠 */
  private static Map<String, Object> slot() {
    return Map.of("x", 10, "y", 10, "width", 30, "height", 40);
  }

  /** 必須項目のみのメッセージ枠 */
  private static Map<String, Object> text() {
    return Map.of("x", 50, "y", 80, "width", 40, "fontFamily", "mincho", "fontSize", 2.5);
  }

  /** 必須項目のみの額縁定義に、指定した項目を上書き・追加した額縁定義 */
  private static FrameDefinition definition(Map<String, Object> overrides) {
    Map<String, Object> value = new HashMap<>();
    value.put("size", Map.of("width", 364, "height", 257));
    value.put("slots", List.of(slot()));
    value.putAll(overrides);
    return new FrameDefinition(value);
  }

  /** 指定した項目を上書き・追加した写真枠1件だけを持つ額縁定義 */
  private static FrameDefinition withSlot(Map<String, Object> overrides) {
    Map<String, Object> value = new HashMap<>(slot());
    value.putAll(overrides);
    return definition(Map.of("slots", List.of(value)));
  }

  /** 指定した項目を上書き・追加したメッセージ枠1件だけを持つ額縁定義 */
  private static FrameDefinition withText(Map<String, Object> overrides) {
    Map<String, Object> value = new HashMap<>(text());
    value.putAll(overrides);
    return definition(Map.of("texts", List.of(value)));
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class isValid {
    @Test
    @Order(1)
    @DisplayName("正常系：必須項目（サイズ・写真枠1件）のみの場合、妥当と判定すること")
    void isValid_requiredOnly() {
      assertTrue(policy.isValid(definition(Map.of())));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：全項目を設定した場合、妥当と判定すること")
    void isValid_allFields() {
      FrameDefinition definition =
          definition(
              Map.of(
                  "background", Map.of("color", "#F5F0E6"),
                  "border", Map.of("color", "#3b2a1a", "width", 4),
                  "slots",
                      List.of(
                          Map.of(
                              "x",
                              5.5,
                              "y",
                              8,
                              "width",
                              40,
                              "height",
                              50,
                              "rotation",
                              -3.5,
                              "borderColor",
                              "#ffffff",
                              "borderWidth",
                              1,
                              "shadow",
                              true)),
                  "texts",
                      List.of(
                          Map.ofEntries(
                              Map.entry("x", 50),
                              Map.entry("y", 80),
                              Map.entry("width", 40),
                              Map.entry("rotation", 0),
                              Map.entry("fontFamily", "script"),
                              Map.entry("fontSize", 3),
                              Map.entry("color", "#000000"),
                              Map.entry("align", "right"),
                              Map.entry("bold", false),
                              Map.entry("italic", true)))));

      assertTrue(policy.isValid(definition));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：未知の項目を含む場合、不正と判定すること")
    void isValid_unknownKey() {
      assertFalse(policy.isValid(definition(Map.of("unknown", 1))));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：サイズが未設定の場合、不正と判定すること")
    void isValid_missingSize() {
      assertFalse(policy.isValid(new FrameDefinition(Map.of("slots", List.of(slot())))));
    }

    @Test
    @Order(5)
    @DisplayName("正常系：サイズが下限（50mm）・上限（3000mm）ちょうどの場合、妥当と判定すること")
    void isValid_sizeAtLimit() {
      assertTrue(policy.isValid(definition(Map.of("size", Map.of("width", 50, "height", 3000)))));
    }

    @Test
    @Order(6)
    @DisplayName("異常系：サイズが下限未満・上限超過の場合、不正と判定すること")
    void isValid_sizeOutOfRange() {
      assertFalse(policy.isValid(definition(Map.of("size", Map.of("width", 49.9, "height", 100)))));
      assertFalse(policy.isValid(definition(Map.of("size", Map.of("width", 100, "height", 3001)))));
    }

    @Test
    @Order(7)
    @DisplayName("異常系：サイズの横幅・縦幅の一方が欠けている、または数値でない場合、不正と判定すること")
    void isValid_sizeInvalidShape() {
      assertFalse(policy.isValid(definition(Map.of("size", Map.of("width", 100)))));
      assertFalse(
          policy.isValid(definition(Map.of("size", Map.of("width", "100", "height", 100)))));
    }

    @Test
    @Order(8)
    @DisplayName("異常系：色が#RRGGBB形式でない場合、不正と判定すること")
    void isValid_invalidColor() {
      assertFalse(policy.isValid(definition(Map.of("background", Map.of("color", "red")))));
      assertFalse(policy.isValid(definition(Map.of("border", Map.of("color", "#fff")))));
      assertFalse(policy.isValid(withSlot(Map.of("borderColor", "#gggggg"))));
      assertFalse(policy.isValid(withText(Map.of("color", "#0000000"))));
    }

    @Test
    @Order(9)
    @DisplayName("正常系：外枠の太さが上限（20%）ちょうどの場合、妥当と判定すること")
    void isValid_borderWidthAtLimit() {
      assertTrue(policy.isValid(definition(Map.of("border", Map.of("width", 20)))));
    }

    @Test
    @Order(10)
    @DisplayName("異常系：外枠の太さが上限超過・負の値の場合、不正と判定すること")
    void isValid_borderWidthOutOfRange() {
      assertFalse(policy.isValid(definition(Map.of("border", Map.of("width", 20.1)))));
      assertFalse(policy.isValid(definition(Map.of("border", Map.of("width", -1)))));
    }

    @Test
    @Order(11)
    @DisplayName("異常系：写真枠が未設定・空の場合、不正と判定すること")
    void isValid_noSlots() {
      assertFalse(
          policy.isValid(new FrameDefinition(Map.of("size", Map.of("width", 364, "height", 257)))));
      assertFalse(policy.isValid(definition(Map.of("slots", List.of()))));
    }

    @Test
    @Order(12)
    @DisplayName("正常系：写真枠が上限（30件）ちょうどの場合、妥当と判定すること")
    void isValid_slotsAtLimit() {
      List<Object> slots = IntStream.range(0, 30).mapToObj(i -> (Object) slot()).toList();

      assertTrue(policy.isValid(definition(Map.of("slots", slots))));
    }

    @Test
    @Order(13)
    @DisplayName("異常系：写真枠が上限超過（31件）の場合、不正と判定すること")
    void isValid_slotsOverLimit() {
      List<Object> slots = IntStream.range(0, 31).mapToObj(i -> (Object) slot()).toList();

      assertFalse(policy.isValid(definition(Map.of("slots", slots))));
    }

    @Test
    @Order(14)
    @DisplayName("異常系：写真枠に未知の項目を含む・必須項目が欠けている場合、不正と判定すること")
    void isValid_slotInvalidKeys() {
      assertFalse(policy.isValid(withSlot(Map.of("unknown", 1))));
      assertFalse(
          policy.isValid(
              definition(Map.of("slots", List.of(Map.of("x", 0, "y", 0, "width", 10))))));
    }

    @Test
    @Order(15)
    @DisplayName("正常系：写真枠の位置が0%・100%、大きさが100%ちょうどの場合、妥当と判定すること")
    void isValid_slotRectAtLimit() {
      assertTrue(policy.isValid(withSlot(Map.of("x", 0, "y", 100, "width", 100, "height", 100))));
    }

    @Test
    @Order(16)
    @DisplayName("異常系：写真枠の位置が範囲外、大きさが0以下・100%超過の場合、不正と判定すること")
    void isValid_slotRectOutOfRange() {
      assertFalse(policy.isValid(withSlot(Map.of("x", -0.1))));
      assertFalse(policy.isValid(withSlot(Map.of("y", 100.1))));
      assertFalse(policy.isValid(withSlot(Map.of("width", 0))));
      assertFalse(policy.isValid(withSlot(Map.of("height", 100.1))));
    }

    @Test
    @Order(17)
    @DisplayName("正常系：傾きが±180度ちょうどの場合、妥当と判定すること")
    void isValid_rotationAtLimit() {
      assertTrue(policy.isValid(withSlot(Map.of("rotation", -180))));
      assertTrue(policy.isValid(withText(Map.of("rotation", 180))));
    }

    @Test
    @Order(18)
    @DisplayName("異常系：傾きが±180度を超える場合、不正と判定すること")
    void isValid_rotationOutOfRange() {
      assertFalse(policy.isValid(withSlot(Map.of("rotation", 180.5))));
      assertFalse(policy.isValid(withText(Map.of("rotation", -181))));
    }

    @Test
    @Order(19)
    @DisplayName("異常系：写真枠の枠線の太さが上限超過、影が真偽値でない場合、不正と判定すること")
    void isValid_slotDecorationInvalid() {
      assertFalse(policy.isValid(withSlot(Map.of("borderWidth", 10.5))));
      assertFalse(policy.isValid(withSlot(Map.of("shadow", "true"))));
    }

    @Test
    @Order(20)
    @DisplayName("正常系：メッセージ枠が上限（10件）ちょうどの場合、妥当と判定すること")
    void isValid_textsAtLimit() {
      List<Object> texts = IntStream.range(0, 10).mapToObj(i -> (Object) text()).toList();

      assertTrue(policy.isValid(definition(Map.of("texts", texts))));
    }

    @Test
    @Order(21)
    @DisplayName("異常系：メッセージ枠が上限超過（11件）、または配列でない場合、不正と判定すること")
    void isValid_textsInvalid() {
      List<Object> texts = IntStream.range(0, 11).mapToObj(i -> (Object) text()).toList();

      assertFalse(policy.isValid(definition(Map.of("texts", texts))));
      assertFalse(policy.isValid(definition(Map.of("texts", text()))));
    }

    @Test
    @Order(22)
    @DisplayName("正常系：選択できるすべてのフォントを妥当と判定すること")
    void isValid_allFontFamilies() {
      FrameDefinitionPolicy.FONT_FAMILIES.forEach(fontFamily ->
          assertTrue(policy.isValid(withText(Map.of("fontFamily", fontFamily))), fontFamily));
    }

    @Test
    @Order(23)
    @DisplayName("異常系：フォントが未設定・選択肢にない場合、不正と判定すること")
    void isValid_invalidFontFamily() {
      Map<String, Object> withoutFont = new HashMap<>(text());
      withoutFont.remove("fontFamily");

      assertFalse(policy.isValid(definition(Map.of("texts", List.of(withoutFont)))));
      assertFalse(policy.isValid(withText(Map.of("fontFamily", "Comic Sans MS"))));
    }

    @Test
    @Order(24)
    @DisplayName("正常系：文字サイズが下限（0.5%）・上限（30%）ちょうどの場合、妥当と判定すること")
    void isValid_fontSizeAtLimit() {
      assertTrue(policy.isValid(withText(Map.of("fontSize", 0.5))));
      assertTrue(policy.isValid(withText(Map.of("fontSize", 30))));
    }

    @Test
    @Order(25)
    @DisplayName("異常系：文字サイズが下限未満・上限超過の場合、不正と判定すること")
    void isValid_fontSizeOutOfRange() {
      assertFalse(policy.isValid(withText(Map.of("fontSize", 0.4))));
      assertFalse(policy.isValid(withText(Map.of("fontSize", 30.1))));
    }

    @Test
    @Order(26)
    @DisplayName("異常系：揃えが選択肢にない、太字・斜体が真偽値でない場合、不正と判定すること")
    void isValid_textDecorationInvalid() {
      assertFalse(policy.isValid(withText(Map.of("align", "justify"))));
      assertFalse(policy.isValid(withText(Map.of("bold", 1))));
      assertFalse(policy.isValid(withText(Map.of("italic", "true"))));
    }

    @Test
    @Order(27)
    @DisplayName("異常系：数値が有限でない（NaN・無限大）場合、不正と判定すること")
    void isValid_notFinite() {
      assertFalse(policy.isValid(withSlot(Map.of("x", Double.NaN))));
      assertFalse(policy.isValid(withText(Map.of("fontSize", Double.POSITIVE_INFINITY))));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class countSlots {
    @Test
    @Order(1)
    @DisplayName("正常系：写真枠の数を返すこと")
    void countSlots_success() {
      assertEquals(2, policy.countSlots(definition(Map.of("slots", List.of(slot(), slot())))));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：写真枠の一覧が未設定・配列でない場合、0を返すこと")
    void countSlots_notList() {
      assertEquals(0, policy.countSlots(new FrameDefinition(Map.of())));
      assertEquals(0, policy.countSlots(new FrameDefinition(Map.of("slots", slot()))));
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class countTexts {
    @Test
    @Order(1)
    @DisplayName("正常系：メッセージ枠の数を返すこと")
    void countTexts_success() {
      assertEquals(1, policy.countTexts(definition(Map.of("texts", List.of(text())))));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：メッセージ枠の一覧が未設定の場合、0を返すこと")
    void countTexts_missing() {
      assertEquals(0, policy.countTexts(definition(Map.of())));
    }
  }
}
