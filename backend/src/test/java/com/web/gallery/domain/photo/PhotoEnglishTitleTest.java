package com.web.gallery.domain.photo;

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
class PhotoEnglishTitleTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class constructor {
    @Test
    @Order(1)
    @DisplayName("正常系：100文字以内の値を指定した場合、インスタンスが生成されること")
    void constructor_success() {
      String value = "a".repeat(100);

      PhotoEnglishTitle actual = new PhotoEnglishTitle(value);

      assertEquals(value, actual.value());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_null() {
      assertThrows(IllegalArgumentException.class, () -> new PhotoEnglishTitle(null));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：100文字を超える値を指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_tooLong() {
      String value = "a".repeat(101);

      assertThrows(IllegalArgumentException.class, () -> new PhotoEnglishTitle(value));
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getOrDefault {
    @Test
    @Order(1)
    @DisplayName("正常系：nullでない場合、そのままの値を返すこと")
    void getOrDefault_notNull() {
      PhotoEnglishTitle title = new PhotoEnglishTitle("Sunset");

      PhotoEnglishTitle actual = PhotoEnglishTitle.getOrDefault(title);

      assertSame(title, actual);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：nullの場合、デフォルト値を返すこと")
    void getOrDefault_null() {
      PhotoEnglishTitle actual = PhotoEnglishTitle.getOrDefault(null);

      assertEquals(new PhotoEnglishTitle(Consts.STRING_EMPTY), actual);
    }
  }
}
