package com.web.gallery.domain.account;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.constant.Consts;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class LastLoginDatetimeTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class constructor {
    @Test
    @Order(1)
    @DisplayName("正常系：nullでない値を指定した場合、インスタンスが生成されること")
    void constructor_success() {
      OffsetDateTime value = OffsetDateTime.now();

      LastLoginDatetime actual = new LastLoginDatetime(value);

      assertEquals(value, actual.value());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_null() {
      assertThrows(IllegalArgumentException.class, () -> new LastLoginDatetime(null));
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
      LastLoginDatetime value = new LastLoginDatetime(OffsetDateTime.now());

      LastLoginDatetime actual = LastLoginDatetime.getOrDefault(value);

      assertSame(value, actual);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：nullの場合、デフォルト値を持つインスタンスを返すこと")
    void getOrDefault_null() {
      LastLoginDatetime actual = LastLoginDatetime.getOrDefault(null);

      assertEquals(Consts.MIN_OFFSET_DATE_TIME, actual.value());
    }
  }
}
