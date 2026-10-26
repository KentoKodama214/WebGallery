package com.web.gallery.domain.model.photo;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class TemplateSettingsTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class constructor {
    @Test
    @Order(1)
    @DisplayName("正常系：入れ子のリスト・マップを含む値を指定した場合、同じ内容でインスタンスが生成されること")
    void constructor_success() {
      Map<String, Object> value =
          Map.of("rows", List.of(Map.of("pattern", "TWO_EQUAL")), "caption", "キャプション");

      TemplateSettings actual = new TemplateSettings(value);

      assertEquals(value, actual.value());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_null() {
      assertThrows(IllegalArgumentException.class, () -> new TemplateSettings(null));
    }

    @Test
    @Order(3)
    @DisplayName("正常系：値がnullの項目・リストのnull要素は、未設定とみなして取り除くこと")
    void constructor_removeNullValues() {
      Map<String, Object> value = new HashMap<>();
      value.put("theme", "テーマ");
      value.put("subTheme", null);
      List<Object> rows = new ArrayList<>();
      rows.add(null);
      rows.add(Map.of("pattern", "TWO_EQUAL"));
      value.put("rows", rows);

      TemplateSettings actual = new TemplateSettings(value);

      assertEquals(Set.of("theme", "rows"), actual.keys());
      assertEquals(List.of(Map.of("pattern", "TWO_EQUAL")), actual.get("rows"));
    }

    @Test
    @Order(4)
    @DisplayName("正常系：生成元のマップ・リストを後から変更しても、インスタンスの値は変わらないこと")
    void constructor_defensiveCopy() {
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("pattern", "TWO_EQUAL");
      List<Object> rows = new ArrayList<>(List.of(row));
      Map<String, Object> value = new LinkedHashMap<>();
      value.put("rows", rows);

      TemplateSettings actual = new TemplateSettings(value);
      value.put("caption", "追加");
      rows.add(Map.of("pattern", "THREE_EQUAL"));
      row.put("pattern", "WIDE_NARROW");

      assertEquals(Map.of("rows", List.of(Map.of("pattern", "TWO_EQUAL"))), actual.value());
    }

    @Test
    @Order(5)
    @DisplayName("異常系：インスタンスの値（入れ子を含む）は変更できないこと")
    void constructor_unmodifiable() {
      TemplateSettings actual =
          new TemplateSettings(Map.of("rows", List.of(Map.of("pattern", "TWO_EQUAL"))));

      assertThrows(UnsupportedOperationException.class, () -> actual.value().put("a", "b"));
      @SuppressWarnings("unchecked")
      List<Object> rows = (List<Object>) actual.get("rows");
      assertThrows(UnsupportedOperationException.class, () -> rows.add("x"));
      @SuppressWarnings("unchecked")
      Map<String, Object> row = (Map<String, Object>) rows.get(0);
      assertThrows(UnsupportedOperationException.class, () -> row.put("pattern", "x"));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class empty {
    @Test
    @Order(1)
    @DisplayName("正常系：項目が1つもないインスタンスを返すこと")
    void empty_success() {
      assertTrue(TemplateSettings.empty().isEmpty());
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getOrDefault {
    @Test
    @Order(1)
    @DisplayName("正常系：nullでない場合、同一インスタンスを返すこと")
    void getOrDefault_nonNull() {
      TemplateSettings settings = new TemplateSettings(Map.of("theme", "テーマ"));

      assertSame(settings, TemplateSettings.getOrDefault(settings));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：nullの場合、項目なしのインスタンスを返すこと")
    void getOrDefault_null() {
      assertEquals(TemplateSettings.empty(), TemplateSettings.getOrDefault(null));
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class get {
    @Test
    @Order(1)
    @DisplayName("正常系：設定されている項目の値を返し、未設定の項目はnullを返すこと")
    void get_success() {
      TemplateSettings settings = new TemplateSettings(Map.of("theme", "テーマ"));

      assertEquals("テーマ", settings.get("theme"));
      assertNull(settings.get("subTheme"));
    }
  }
}
