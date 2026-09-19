package com.web.gallery.domain.account;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.constant.Consts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class BirthplacePrefectureKbnCodeTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class constructor {
    @Test
    @Order(1)
    @DisplayName("正常系：nullでない値を指定した場合、インスタンスが生成されること")
    void constructor_success() {
      BirthplacePrefectureKbnCode actual = new BirthplacePrefectureKbnCode("13");

      assertEquals("13", actual.value());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_null() {
      assertThrows(IllegalArgumentException.class, () -> new BirthplacePrefectureKbnCode(null));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getOrDefault {
    @Test
    @Order(1)
    @DisplayName("正常系：nullでない場合、そのまま返すこと")
    void getOrDefault_notNull() {
      BirthplacePrefectureKbnCode value = new BirthplacePrefectureKbnCode("13");

      BirthplacePrefectureKbnCode actual = BirthplacePrefectureKbnCode.getOrDefault(value);

      assertSame(value, actual);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：nullの場合、デフォルト値を持つインスタンスを返すこと")
    void getOrDefault_null() {
      BirthplacePrefectureKbnCode actual = BirthplacePrefectureKbnCode.getOrDefault(null);

      assertEquals(Consts.STRING_NONE, actual.value());
    }
  }
}
