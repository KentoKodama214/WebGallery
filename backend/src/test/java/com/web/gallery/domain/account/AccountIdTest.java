package com.web.gallery.domain.account;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class AccountIdTest {

  @Test
  @DisplayName("正常系：半角英数字8〜20文字を指定した場合、インスタンスが生成されること")
  void constructor_success() {
    AccountId actual = new AccountId("abcDEF12");

    assertEquals("abcDEF12", actual.value());
  }

  @Test
  @DisplayName("正常系：半角英数字20文字（上限）を指定した場合、インスタンスが生成されること")
  void constructor_maxLength() {
    AccountId actual = new AccountId("a1234567890123456789");

    assertEquals("a1234567890123456789", actual.value());
  }

  @Test
  @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
  void constructor_null() {
    assertThrows(IllegalArgumentException.class, () -> new AccountId(null));
  }

  @Test
  @DisplayName("異常系：7文字（下限未満）を指定した場合、IllegalArgumentExceptionをスローすること")
  void constructor_tooShort() {
    assertThrows(IllegalArgumentException.class, () -> new AccountId("a123456"));
  }

  @Test
  @DisplayName("異常系：21文字（上限超過）を指定した場合、IllegalArgumentExceptionをスローすること")
  void constructor_tooLong() {
    assertThrows(IllegalArgumentException.class, () -> new AccountId("a12345678901234567890"));
  }

  @Test
  @DisplayName("異常系：半角英数字以外を含む場合、IllegalArgumentExceptionをスローすること")
  void constructor_invalidCharacter() {
    assertThrows(IllegalArgumentException.class, () -> new AccountId("abcDEF1-"));
  }
}
