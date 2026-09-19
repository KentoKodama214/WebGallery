package com.web.gallery.domain.photo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class FavoriteCountTest {

  @Test
  @DisplayName("正常系：0以上の値を指定した場合、インスタンスが生成されること")
  void constructor_success() {
    FavoriteCount actual = new FavoriteCount(0);

    assertEquals(0, actual.value());
  }

  @Test
  @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
  void constructor_null() {
    assertThrows(IllegalArgumentException.class, () -> new FavoriteCount(null));
  }

  @Test
  @DisplayName("異常系：負の値を指定した場合、IllegalArgumentExceptionをスローすること")
  void constructor_negative() {
    assertThrows(IllegalArgumentException.class, () -> new FavoriteCount(-1));
  }
}
