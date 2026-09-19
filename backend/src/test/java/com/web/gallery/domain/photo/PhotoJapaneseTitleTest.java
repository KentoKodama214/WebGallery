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
class PhotoJapaneseTitleTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class constructor {
    @Test
    @Order(1)
    @DisplayName("正常系：100文字以内の値を指定した場合、インスタンスが生成されること")
    void constructor_success() {
      String value = "あ".repeat(100);

      PhotoJapaneseTitle actual = new PhotoJapaneseTitle(value);

      assertEquals(value, actual.value());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：nullを指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_null() {
      assertThrows(IllegalArgumentException.class, () -> new PhotoJapaneseTitle(null));
    }

    @Test
    @Order(3)
    @DisplayName("異常系：100文字を超える値を指定した場合、IllegalArgumentExceptionをスローすること")
    void constructor_tooLong() {
      String value = "あ".repeat(101);

      assertThrows(IllegalArgumentException.class, () -> new PhotoJapaneseTitle(value));
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
      PhotoJapaneseTitle title = new PhotoJapaneseTitle("夕焼け");

      PhotoJapaneseTitle actual = PhotoJapaneseTitle.getOrDefault(title);

      assertSame(title, actual);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：nullの場合、デフォルト値を返すこと")
    void getOrDefault_null() {
      PhotoJapaneseTitle actual = PhotoJapaneseTitle.getOrDefault(null);

      assertEquals(new PhotoJapaneseTitle(Consts.STRING_EMPTY), actual);
    }
  }
}
