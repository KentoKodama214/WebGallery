package com.web.gallery.model;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.photo.PhotoNo;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PhotoNoListTest {

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：写真番号のLongリストが、それぞれPhotoNoへ変換されて反映されること")
    void from_allFieldsSet() {
      PhotoNoList actual = PhotoNoList.from(List.of(1L, 2L, 3L));

      assertEquals(3, actual.size());
      assertEquals(new PhotoNo(1L), actual.get(0));
      assertEquals(new PhotoNo(2L), actual.get(1));
      assertEquals(new PhotoNo(3L), actual.get(2));
    }

    @Test
    @Order(2)
    @DisplayName("正常系：空リストの場合、空のPhotoNoListが返ること")
    void from_emptyList() {
      PhotoNoList actual = PhotoNoList.from(List.of());

      assertTrue(actual.isEmpty());
    }
  }
}
