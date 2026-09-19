package com.web.gallery.domain.account;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class AccountNoTest {

  @Test
  @DisplayName("正常系：正の値を指定した場合、インスタンスが生成されること")
  void constructor_success() {
    AccountNo actual = new AccountNo(1L);

    assertEquals(1L, actual.value());
  }

  @Test
  @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
  void constructor_null() {
    assertThrows(IllegalArgumentException.class, () -> new AccountNo(null));
  }

  @Test
  @DisplayName("異常系：0を指定した場合、IllegalArgumentExceptionをスローすること")
  void constructor_zero() {
    assertThrows(IllegalArgumentException.class, () -> new AccountNo(0L));
  }

  @Test
  @DisplayName("異常系：負の値を指定した場合、IllegalArgumentExceptionをスローすること")
  void constructor_negative() {
    assertThrows(IllegalArgumentException.class, () -> new AccountNo(-1L));
  }
}
