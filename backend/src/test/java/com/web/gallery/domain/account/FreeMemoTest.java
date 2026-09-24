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
class FreeMemoTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class toStringMethod {
    @Test
    @Order(1)
    @DisplayName("正常系：valueがnullでない場合、そのまま返すこと")
    void toString_notNull() {
      FreeMemo actual = new FreeMemo("メモ");

      assertEquals("メモ", actual.toString());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：valueがnullの場合、空文字を返すこと")
    void toString_null() {
      FreeMemo actual = new FreeMemo(null);

      assertEquals("", actual.toString());
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
      FreeMemo value = new FreeMemo("メモ");

      FreeMemo actual = FreeMemo.getOrDefault(value);

      assertSame(value, actual);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：nullの場合、デフォルト値を持つインスタンスを返すこと")
    void getOrDefault_null() {
      FreeMemo actual = FreeMemo.getOrDefault(null);

      assertEquals(Consts.STRING_EMPTY, actual.value());
    }
  }
}
