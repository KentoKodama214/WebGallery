package com.web.gallery.domain.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class KbnClassJapaneseNameTest {

  @Test
  @DisplayName("正常系：nullでない値を指定した場合、インスタンスが生成されること")
  void constructor_success() {
    KbnClassJapaneseName actual = new KbnClassJapaneseName("分類名");

    assertEquals("分類名", actual.value());
  }

  @Test
  @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
  void constructor_null() {
    assertThrows(IllegalArgumentException.class, () -> new KbnClassJapaneseName(null));
  }
}
