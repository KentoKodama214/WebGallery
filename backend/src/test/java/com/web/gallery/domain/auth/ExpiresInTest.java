package com.web.gallery.domain.auth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class ExpiresInTest {

  @Test
  @DisplayName("正常系：0を指定した場合、インスタンスが生成されること")
  void constructor_zero() {
    ExpiresIn actual = new ExpiresIn(0L);

    assertEquals(0L, actual.value());
  }

  @Test
  @DisplayName("正常系：正の値を指定した場合、インスタンスが生成されること")
  void constructor_positive() {
    ExpiresIn actual = new ExpiresIn(3600L);

    assertEquals(3600L, actual.value());
  }

  @Test
  @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
  void constructor_null() {
    assertThrows(IllegalArgumentException.class, () -> new ExpiresIn(null));
  }

  @Test
  @DisplayName("異常系：負の値を指定した場合、IllegalArgumentExceptionをスローすること")
  void constructor_negative() {
    assertThrows(IllegalArgumentException.class, () -> new ExpiresIn(-1L));
  }
}
