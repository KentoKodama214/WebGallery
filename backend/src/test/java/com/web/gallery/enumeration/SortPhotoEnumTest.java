package com.web.gallery.enumeration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class SortPhotoEnumTest {

  @Test
  @DisplayName("正常系：クエリパラメータ値に一致する場合、対応するEnum値を返すこと")
  void getOrDefault_matchedByQueryValue() {
    assertEquals(SortPhotoEnum.FAVORITE, SortPhotoEnum.getOrDefault("favorite"));
  }

  @Test
  @DisplayName("正常系：Enum名に一致する場合、対応するEnum値を返すこと")
  void getOrDefault_matchedByName() {
    assertEquals(SortPhotoEnum.SEASON, SortPhotoEnum.getOrDefault("SEASON"));
  }

  @Test
  @DisplayName("異常系：nullを指定した場合、PHOTO_ATを返すこと")
  void getOrDefault_null() {
    assertEquals(SortPhotoEnum.PHOTO_AT, SortPhotoEnum.getOrDefault(null));
  }

  @Test
  @DisplayName("異常系：該当するEnum値がない場合、PHOTO_ATを返すこと")
  void getOrDefault_unmatched() {
    assertEquals(SortPhotoEnum.PHOTO_AT, SortPhotoEnum.getOrDefault("unknown"));
  }
}
