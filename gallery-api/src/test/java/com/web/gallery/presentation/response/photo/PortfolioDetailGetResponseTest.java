package com.web.gallery.presentation.response.photo;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.application.model.photo.PortfolioDetailModel;
import com.web.gallery.application.model.photo.PortfolioPhotoModel;
import com.web.gallery.application.model.photo.PortfolioPhotoModelList;
import com.web.gallery.application.model.photo.PortfolioTagModel;
import com.web.gallery.application.model.photo.PortfolioTagModelList;
import com.web.gallery.domain.enumeration.DirectionEnum;
import com.web.gallery.domain.enumeration.PortfolioStatusEnum;
import com.web.gallery.domain.enumeration.PortfolioTemplateEnum;
import com.web.gallery.domain.model.account.AccountNo;
import com.web.gallery.domain.model.photo.DisplayOrder;
import com.web.gallery.domain.model.photo.ImageFilePath;
import com.web.gallery.domain.model.photo.PhotoJapaneseTitle;
import com.web.gallery.domain.model.photo.PhotoNo;
import com.web.gallery.domain.model.photo.PortfolioDescription;
import com.web.gallery.domain.model.photo.PortfolioNo;
import com.web.gallery.domain.model.photo.PortfolioTheme;
import com.web.gallery.domain.model.photo.TagEnglishName;
import com.web.gallery.domain.model.photo.TagJapaneseName;
import com.web.gallery.domain.model.photo.TagNo;
import com.web.gallery.domain.model.photo.TemplateSettings;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PortfolioDetailGetResponseTest {

  private static final OffsetDateTime CREATED_AT =
      OffsetDateTime.of(2026, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(9));
  private static final OffsetDateTime UPDATED_AT =
      OffsetDateTime.of(2026, 2, 1, 9, 0, 0, 0, ZoneOffset.ofHours(9));

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class from {
    @Test
    @Order(1)
    @DisplayName("正常系：全項目が設定されている場合、写真・タグを含めてそのまま値が転写されること")
    void from_allFieldsSet() {
      PortfolioDetailModel model =
          PortfolioDetailModel.builder()
              .accountNo(new AccountNo(1L))
              .portfolioNo(new PortfolioNo(2L))
              .theme(new PortfolioTheme("日本の自然"))
              .description(new PortfolioDescription("説明文"))
              .templateKbn(PortfolioTemplateEnum.GRID)
              .templateSettings(
                  new TemplateSettings(Map.of("rows", List.of(Map.of("pattern", "TWO_EQUAL")))))
              .statusKbn(PortfolioStatusEnum.PUBLISHED)
              .createdAt(CREATED_AT)
              .updatedAt(UPDATED_AT)
              .portfolioPhotoModelList(
                  PortfolioPhotoModelList.of(
                      List.of(
                          PortfolioPhotoModel.builder()
                              .photoNo(new PhotoNo(3L))
                              .displayOrder(new DisplayOrder(1))
                              .photoSettings(new TemplateSettings(Map.of("caption", "金閣寺")))
                              .photoJapaneseTitle(new PhotoJapaneseTitle("金閣寺"))
                              .imageFilePath(new ImageFilePath("https://signed/1/3.jpg"))
                              .directionKbn(DirectionEnum.HORIZONTAL)
                              .build())))
              .portfolioTagModelList(
                  PortfolioTagModelList.of(
                      List.of(
                          PortfolioTagModel.builder()
                              .tagNo(new TagNo(1L))
                              .tagJapaneseName(new TagJapaneseName("自然"))
                              .tagEnglishName(new TagEnglishName("nature"))
                              .build())))
              .build();

      PortfolioDetailGetResponse actual = PortfolioDetailGetResponse.from(model);

      assertEquals(2L, actual.getPortfolioNo());
      assertEquals("日本の自然", actual.getTheme());
      assertEquals("説明文", actual.getDescription());
      assertEquals(PortfolioTemplateEnum.GRID, actual.getTemplateKbn());
      assertEquals(
          Map.of("rows", List.of(Map.of("pattern", "TWO_EQUAL"))), actual.getTemplateSettings());
      assertEquals(PortfolioStatusEnum.PUBLISHED, actual.getStatusKbn());
      assertEquals(CREATED_AT, actual.getCreatedAt());
      assertEquals(UPDATED_AT, actual.getUpdatedAt());

      assertEquals(1, actual.getPhotoList().size());
      PortfolioPhotoItemResponse photo = actual.getPhotoList().get(0);
      assertEquals(3L, photo.getPhotoNo());
      assertEquals(1, photo.getDisplayOrder());
      assertEquals(Map.of("caption", "金閣寺"), photo.getPhotoSettings());
      assertEquals("金閣寺", photo.getPhotoJapaneseTitle());
      assertEquals("https://signed/1/3.jpg", photo.getImageFilePath());
      assertEquals(DirectionEnum.HORIZONTAL, photo.getDirectionKbn());

      assertEquals(1, actual.getTagList().size());
      PortfolioTagItemResponse tag = actual.getTagList().get(0);
      assertEquals(1L, tag.getTagNo());
      assertEquals("自然", tag.getTagJapaneseName());
      assertEquals("nature", tag.getTagEnglishName());
    }

    @Test
    @Order(2)
    @DisplayName("異常系：写真・タグが空の場合、空リストとなること")
    void from_emptyLists() {
      PortfolioDetailModel model =
          PortfolioDetailModel.builder()
              .accountNo(new AccountNo(1L))
              .portfolioNo(new PortfolioNo(2L))
              .theme(new PortfolioTheme("日本の自然"))
              .description(new PortfolioDescription(""))
              .templateKbn(PortfolioTemplateEnum.FEATURE)
              .templateSettings(TemplateSettings.empty())
              .statusKbn(PortfolioStatusEnum.DRAFT)
              .portfolioPhotoModelList(PortfolioPhotoModelList.empty())
              .portfolioTagModelList(PortfolioTagModelList.empty())
              .build();

      PortfolioDetailGetResponse actual = PortfolioDetailGetResponse.from(model);

      assertEquals("", actual.getDescription());
      assertEquals(Map.of(), actual.getTemplateSettings());
      assertTrue(actual.getPhotoList().isEmpty());
      assertTrue(actual.getTagList().isEmpty());
      assertNull(actual.getCreatedAt());
      assertNull(actual.getUpdatedAt());
    }
  }
}
