package com.web.gallery.domain.inquiry;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class InquirySubjectTest {

  @Test
  @DisplayName("正常系：空白でない値を指定した場合、インスタンスが生成されること")
  void constructor_success() {
    InquirySubject actual = new InquirySubject("お問い合わせ件名");

    assertEquals("お問い合わせ件名", actual.value());
  }

  @Test
  @DisplayName("正常系：100文字（上限）を指定した場合、インスタンスが生成されること")
  void constructor_maxLength() {
    String value = "a".repeat(100);

    InquirySubject actual = new InquirySubject(value);

    assertEquals(value, actual.value());
  }

  @Test
  @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
  void constructor_null() {
    assertThrows(IllegalArgumentException.class, () -> new InquirySubject(null));
  }

  @Test
  @DisplayName("異常系：空白文字のみを指定した場合、IllegalArgumentExceptionをスローすること")
  void constructor_blank() {
    assertThrows(IllegalArgumentException.class, () -> new InquirySubject("   "));
  }

  @Test
  @DisplayName("異常系：101文字（上限超過）を指定した場合、IllegalArgumentExceptionをスローすること")
  void constructor_tooLong() {
    String value = "a".repeat(101);

    assertThrows(IllegalArgumentException.class, () -> new InquirySubject(value));
  }
}
