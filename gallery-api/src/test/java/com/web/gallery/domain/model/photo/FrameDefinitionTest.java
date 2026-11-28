package com.web.gallery.domain.model.photo;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
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
class FrameDefinitionTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class constructor {
    @Test
    @Order(1)
    @DisplayName("正常系：入れ子のマップ・リストを含む値を指定した場合、そのままの内容で生成されること")
    void constructor_success() {
      Map<String, Object> value =
          Map.of(
              "size", Map.of("width", 364, "height", 257),
              "slots", List.of(Map.of("x", 10, "y", 10, "width", 30, "height", 40)));

      assertEquals(value, new FrameDefinition(value).value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：値がnullの項目は取り除かれること")
    void constructor_nullValueRemoved() {
      Map<String, Object> value = new HashMap<>();
      value.put("size", Map.of("width", 364, "height", 257));
      value.put("background", null);

      assertEquals(
          Map.of("size", Map.of("width", 364, "height", 257)), new FrameDefinition(value).value());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：生成後に元のマップ・リストを変更しても、値は変わらず、変更もできないこと")
    void constructor_immutableCopy() {
      List<Object> slots = new ArrayList<>(List.of(Map.of("x", 10)));
      Map<String, Object> value = new HashMap<>(Map.of("slots", slots));
      FrameDefinition definition = new FrameDefinition(value);

      slots.add(Map.of("x", 20));
      value.put("texts", List.of());

      assertEquals(Map.of("slots", List.of(Map.of("x", 10))), definition.value());
      assertThrows(
          UnsupportedOperationException.class, () -> definition.value().put("texts", List.of()));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_null() {
      assertThrows(IllegalArgumentException.class, () -> new FrameDefinition(null));
    }
  }
}
