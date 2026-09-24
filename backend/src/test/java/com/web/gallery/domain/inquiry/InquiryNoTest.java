package com.web.gallery.domain.inquiry;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class InquiryNoTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class constructor {
    @Test
    @Order(1)
    @DisplayName("正常系：正の値を指定した場合、インスタンスが生成されること")
    void constructor_success() {
      InquiryNo actual = new InquiryNo(1L);

      assertEquals(1L, actual.value());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_null() {
      assertThrows(IllegalArgumentException.class, () -> new InquiryNo(null));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：0を指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_zero() {
      assertThrows(IllegalArgumentException.class, () -> new InquiryNo(0L));
    }

    @Test
    @Order(4)
    @DisplayName("異常系：負の値を指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_negative() {
      assertThrows(IllegalArgumentException.class, () -> new InquiryNo(-1L));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class next {
    @Test
    @Order(1)
    @DisplayName("正常系：現在の最大お問い合わせ番号がnullの場合、1を返すこと")
    void next_noExisting() {
      InquiryNo actual = InquiryNo.next(null);

      assertEquals(1L, actual.value());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：現在の最大お問い合わせ番号がある場合、それに1を加えた値を返すこと")
    void next_existing() {
      InquiryNo actual = InquiryNo.next(5L);

      assertEquals(6L, actual.value());
    }
  }
}
