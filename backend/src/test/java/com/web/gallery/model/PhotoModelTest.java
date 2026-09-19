package com.web.gallery.model;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import com.web.gallery.domain.photo.TagJapaneseName;
import com.web.gallery.dto.PhotoDto;
import com.web.gallery.entity.PhotoTagMst;
import com.web.gallery.enumeration.DirectionEnum;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PhotoModelTest {

  private PhotoDto baseDto() {
    PhotoDto dto = new PhotoDto();
    dto.setAccountNo(1L);
    dto.setPhotoNo(2L);
    dto.setFavoriteCount(3);
    dto.setIsFavorite(true);
    dto.setPhotoAt(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
    dto.setImageFilePath("path/to/image.jpg");
    dto.setCaption("キャプション");
    dto.setDirectionKbn(DirectionEnum.VERTICAL);
    return dto;
  }

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が値オブジェクトへ変換されてそのまま反映されること")
    void from_allFieldsSet() {
      PhotoDto dto = baseDto();

      PhotoModel actual = PhotoModel.from(dto, List.of());

      assertEquals(new AccountNo(1L), actual.getAccountNo());
      assertEquals(new PhotoNo(2L), actual.getPhotoNo());
      assertEquals(3, actual.getFavoriteCount().value());
      assertTrue(actual.getIsFavorite().value());
      assertNotNull(actual.getPhotoAt());
      assertEquals("path/to/image.jpg", actual.getImageFilePath().value());
      assertEquals("キャプション", actual.getCaption().value());
      assertEquals(DirectionEnum.VERTICAL, actual.getDirectionKbn());
      assertTrue(actual.getPhotoTagModelList().isEmpty());
    }

    @Test
    @Order(2)
    @DisplayName("正常系：撮影日時がJSTに変換されて反映されること")
    void from_photoAtConvertedToJst() {
      PhotoDto dto = baseDto();
      dto.setPhotoAt(OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));

      PhotoModel actual = PhotoModel.from(dto, List.of());

      assertEquals(ZoneOffset.ofHours(9), actual.getPhotoAt().value().getOffset());
    }

    @Test
    @Order(3)
    @DisplayName("正常系：タグエンティティリストのうち該当写真のタグのみがフィルタされて反映されること")
    void from_filtersTagsByPhoto() {
      PhotoDto dto = baseDto();
      PhotoTagMst matched =
          PhotoTagMst.builder()
              .accountNo(1L)
              .photoNo(2L)
              .tagNo(1L)
              .tagJapaneseName("太陽")
              .tagEnglishName("sun")
              .build();
      PhotoTagMst other =
          PhotoTagMst.builder()
              .accountNo(1L)
              .photoNo(999L)
              .tagNo(2L)
              .tagJapaneseName("海")
              .tagEnglishName("sea")
              .build();

      PhotoModel actual = PhotoModel.from(dto, List.of(matched, other));

      assertEquals(1, actual.getPhotoTagModelList().size());
      assertEquals(
          new TagJapaneseName("太陽"), actual.getPhotoTagModelList().get(0).getTagJapaneseName());
    }
  }
}
