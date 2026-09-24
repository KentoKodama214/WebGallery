package com.web.gallery.enumeration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class DirectionEnumTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getOrDefaultByString {
    @Test
    @Order(1)
    @DisplayName("正常系：DB保存値に一致する場合、対応するEnum値を返すこと")
    void getOrDefaultByString_matchedByDbValue() {
      assertEquals(DirectionEnum.VERTICAL, DirectionEnum.getOrDefault("vertical"));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：Enum名に一致する場合、対応するEnum値を返すこと")
    void getOrDefaultByString_matchedByName() {
      assertEquals(DirectionEnum.HORIZONTAL, DirectionEnum.getOrDefault("HORIZONTAL"));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：nullを指定した場合、NONEを返すこと")
    void getOrDefaultByString_null() {
      assertEquals(DirectionEnum.NONE, DirectionEnum.getOrDefault((String) null));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：該当するEnum値がない場合、NONEを返すこと")
    void getOrDefaultByString_unmatched() {
      assertEquals(DirectionEnum.NONE, DirectionEnum.getOrDefault("unknown"));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getOrDefaultByDirectionEnum {
    @Test
    @Order(1)
    @DisplayName("正常系：非nullを指定した場合、そのまま返すこと")
    void getOrDefaultByDirectionEnum_nonNull() {
      assertSame(DirectionEnum.SQUARE, DirectionEnum.getOrDefault(DirectionEnum.SQUARE));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：nullを指定した場合、NONEを返すこと")
    void getOrDefaultByDirectionEnum_null() {
      assertEquals(DirectionEnum.NONE, DirectionEnum.getOrDefault((DirectionEnum) null));
    }
  }
}
