package com.web.gallery.domain.inquiry;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class ReplyBodyTest {

  @Test
  @DisplayName("正常系：空白でない値を指定した場合、インスタンスが生成されること")
  void constructor_success() {
    ReplyBody actual = new ReplyBody("返信内容");

    assertEquals("返信内容", actual.value());
  }

  @Test
  @DisplayName("正常系：2000文字（上限）を指定した場合、インスタンスが生成されること")
  void constructor_maxLength() {
    String value = "a".repeat(2000);

    ReplyBody actual = new ReplyBody(value);

    assertEquals(value, actual.value());
  }

  @Test
  @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
  void constructor_null() {
    assertThrows(IllegalArgumentException.class, () -> new ReplyBody(null));
  }

  @Test
  @DisplayName("異常系：空白文字のみを指定した場合、IllegalArgumentExceptionをスローすること")
  void constructor_blank() {
    assertThrows(IllegalArgumentException.class, () -> new ReplyBody("   "));
  }

  @Test
  @DisplayName("異常系：2001文字（上限超過）を指定した場合、IllegalArgumentExceptionをスローすること")
  void constructor_tooLong() {
    String value = "a".repeat(2001);

    assertThrows(IllegalArgumentException.class, () -> new ReplyBody(value));
  }
}
