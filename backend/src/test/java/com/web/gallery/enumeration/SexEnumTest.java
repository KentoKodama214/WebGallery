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
class SexEnumTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getOrDefaultByString {
    @Test
    @Order(1)
    @DisplayName("正常系：有効な値を指定した場合、対応するEnum値を返すこと")
    void getOrDefaultByString_valid() {
      assertEquals(SexEnum.MAN, SexEnum.getOrDefault("man"));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：nullを指定した場合、NONEを返すこと")
    void getOrDefaultByString_null() {
      assertEquals(SexEnum.NONE, SexEnum.getOrDefault((String) null));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：該当するEnum値がない場合、NONEを返すこと")
    void getOrDefaultByString_invalid() {
      assertEquals(SexEnum.NONE, SexEnum.getOrDefault("invalid"));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getOrDefaultBySexEnum {
    @Test
    @Order(1)
    @DisplayName("正常系：非nullを指定した場合、そのまま返すこと")
    void getOrDefaultBySexEnum_nonNull() {
      assertEquals(SexEnum.WOMAN, SexEnum.getOrDefault(SexEnum.WOMAN));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：nullを指定した場合、NONEを返すこと")
    void getOrDefaultBySexEnum_null() {
      assertEquals(SexEnum.NONE, SexEnum.getOrDefault((SexEnum) null));
    }
  }
}
