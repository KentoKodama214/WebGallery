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
class AuthorityEnumTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getOrDefault {
    @Test
    @Order(1)
    @DisplayName("正常系：DB保存値に一致する場合、対応するEnum値を返すこと")
    void getOrDefault_matchedByDbValue() {
      assertEquals(AuthorityEnum.ADMINISTRATOR, AuthorityEnum.getOrDefault("administrator"));
    }

    @Test
    @Order(2)
    @DisplayName("異常系：nullを指定した場合、MINIを返すこと")
    void getOrDefault_null() {
      assertEquals(AuthorityEnum.MINI, AuthorityEnum.getOrDefault(null));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：該当するEnum値がない場合、MINIを返すこと")
    void getOrDefault_unmatched() {
      assertEquals(AuthorityEnum.MINI, AuthorityEnum.getOrDefault("unknown"));
    }
  }
}
