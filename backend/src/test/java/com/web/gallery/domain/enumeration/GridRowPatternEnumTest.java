package com.web.gallery.domain.enumeration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class GridRowPatternEnumTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class fromName {
    @Test
    @Order(1)
    @DisplayName("正常系：Enum名に一致する場合、対応するEnum値を返すこと")
    void fromName_matched() {
      assertEquals(GridRowPatternEnum.WIDE_NARROW, GridRowPatternEnum.fromName("WIDE_NARROW"));
      assertEquals(GridRowPatternEnum.NARROW_WIDE, GridRowPatternEnum.fromName("NARROW_WIDE"));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：nullを指定した場合、nullを返すこと")
    void fromName_null() {
      assertNull(GridRowPatternEnum.fromName(null));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：該当するEnum値がない場合（大文字小文字の違いを含む）、nullを返すこと")
    void fromName_unmatched() {
      assertNull(GridRowPatternEnum.fromName("two_equal"));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getPhotoCount {
    @Test
    @Order(1)
    @DisplayName("正常系：各行パターンが1行に配置する写真の枚数を返すこと")
    void getPhotoCount_success() {
      assertEquals(2, GridRowPatternEnum.TWO_EQUAL.getPhotoCount());
      assertEquals(3, GridRowPatternEnum.THREE_EQUAL.getPhotoCount());
      assertEquals(2, GridRowPatternEnum.WIDE_NARROW.getPhotoCount());
      assertEquals(2, GridRowPatternEnum.NARROW_WIDE.getPhotoCount());
    }
  }
}
