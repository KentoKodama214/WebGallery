package com.web.gallery.domain.inquiry;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class InquiryIdTest {

  @Test
  @DisplayName("正常系：正の値を指定した場合、インスタンスが生成されること")
  void constructor_success() {
    InquiryId actual = new InquiryId(1L);

    assertEquals(1L, actual.value());
  }

  @Test
  @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
  void constructor_null() {
    assertThrows(IllegalArgumentException.class, () -> new InquiryId(null));
  }

  @Test
  @DisplayName("異常系：0を指定した場合、IllegalArgumentExceptionをスローすること")
  void constructor_zero() {
    assertThrows(IllegalArgumentException.class, () -> new InquiryId(0L));
  }

  @Test
  @DisplayName("異常系：負の値を指定した場合、IllegalArgumentExceptionをスローすること")
  void constructor_negative() {
    assertThrows(IllegalArgumentException.class, () -> new InquiryId(-1L));
  }
}
